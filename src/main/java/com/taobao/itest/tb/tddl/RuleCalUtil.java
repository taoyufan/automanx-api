package com.taobao.itest.tb.tddl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

/**
 * shard rule calculate util, if there have excel need to be shard, the
 * <method>calDataSet()</method> method can shard the data into right
 * dataSources and tables by the specified rules
 * 
 * @author junliang
 */
public class RuleCalUtil {

	private static final String LONE_VALUE = ".longValue()";
	private static final String HASH_CODE = ".hashCode()";

	private static Date _now = new Date();

	public static Map<String, HSSFWorkbook> calDataSet(AppRule rootRule,
			HSSFWorkbook sourceSet, Date now) {
		Map<String, HSSFWorkbook> result = new HashMap<String, HSSFWorkbook>();
		if (now != null) {
			_now = now;
		}
		/**
		 * 1.循环rootRule.rootMap，主备库配置，每个元素对应一份sourceSet；
		 * 2.循环ShardRule.tableRules，对每个表的数据进行分库，分表运算
		 * a.根据tableRule.dbIndexes和tableRule.tbSuffix初始化result Map
		 * b.根据tableRule.dbRules对sourceSet中的数据进行分库运算（先判断不分库的情况），运算结果存储在tempSet中
		 * c.根据tableRule.tbRules对tempSet中的数据进行分表运算（先判断不分表的情况），运算结果存储在result中
		 */
		Map<String, ShardRule> rootMap = rootRule.getRootMap();
		for (ShardRule shardRule : rootMap.values()) {
			Map<String, TableRule> tableRules = shardRule.getTableRules();
			int sNum = sourceSet.getNumberOfSheets();
			for (int i = 0; i < sNum; i++) {
				HSSFSheet sheet = sourceSet.getSheetAt(i);
				if (sheet.getPhysicalNumberOfRows() < 1) {// this sheet has no
															// data, ignore it
					continue;
				}
				String sheetName = sourceSet.getSheetName(i);
				TableRule tableRule = tableRules.get(sheetName);
				if (tableRule == null) {
					throw new RuntimeException(
							"there has no rule for table ["
									+ sheetName
									+ "], the rule config only for these tables \n"
									+ tableRules.keySet()
									+ ",\n please check your rule configuration or data preparation!");
				}
				if (tableRule.getLogicTableName() == null) {
					if (tableRule.isUniqueTB()
							&& tableRule.getUniqueTB() != null
							&& !"".equals(tableRule.getUniqueTB())) {
						tableRule.setLogicTableName(tableRule.getUniqueTB());
					} else {
						tableRule.setLogicTableName(sheetName);
					}
				}
				dealSheetData(result, tableRule, sheet);
			}
		}

		return result;
	}

	private static void dealSheetData(Map<String, HSSFWorkbook> result,
			TableRule tableRule, HSSFSheet sheet) {
		HSSFRow fieldNames = sheet.getRow(0);
		String targetDsName = null;
		String targetTbName = null;
		int rowNum = sheet.getPhysicalNumberOfRows();
		for (int i = 1; i < rowNum; i++) {
			HSSFRow data = sheet.getRow(i);
			if (tableRule.isUniqueDB()) {
				targetDsName = tableRule.getUniqueDB();
			} else {
				String dbRule = decideRule(tableRule.getDbRules(), fieldNames,
						data);
				int dbIndex = calRule(dbRule, fieldNames, data);
				targetDsName = tableRule.getDbIndexes()[dbIndex];
			}

			if (tableRule.isUniqueTB()) {
				targetTbName = tableRule.getUniqueTB();
			} else {
				String tbRule = decideRule(tableRule.getTbRules(), fieldNames,
						data);
				int tbIndex = calRule(tbRule, fieldNames, data);
				Map<String, List<String>> dbTableMap = tableRule
						.getDbTableNames();
				targetTbName = dbTableMap.get(targetDsName).get(tbIndex);
			}
			HSSFWorkbook workbook = result.get(targetDsName);
			if (workbook == null) {
				workbook = new HSSFWorkbook();
			}
			addRow2Workbook(workbook, targetTbName, fieldNames, data);
			result.put(targetDsName, workbook);
		}
	}

	private static String decideRule(String[] rules, HSSFRow fieldNames,
			HSSFRow data) {
		Map<String, Integer> fieldMap = converFieldName2Map(fieldNames);
		if (rules.length == 1) {// 只有一条，直接返回
			return rules[0];
		}
		for (String rule : rules) {// 有多条，返回rules中下标最小且变量数据都存在的规则
			Map<String, String[]> varMap = getVariableMap(rule);
			if (varMap.size() == 0) {// 规则中没有变量，直接返回
				return rule;
			}
			Collection<String[]> values = varMap.values();
			boolean flag = false;// 是否有在数据中找不到的变量
			for (String[] value : values) {
				String fieldName = value[0];
				Integer index = fieldMap.get(fieldName);
				if (index == null) {
					flag = true;
					break;
				}
				HSSFCell cell = data.getCell(index);
				if (cell == null || cell.toString() == null
						|| "".equals(cell.toString())) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return rule;
			}
		}
		throw new RuntimeException(
				"rule config or data prepare is invalid:rules="
						+ Arrays.toString(rules) + " data="
						+ convertRow2String(data));
	}

	private static String convertRow2String(HSSFRow row) {
		StringBuilder sb = new StringBuilder("{");
		Iterator<Cell> it = row.cellIterator();
		while (it.hasNext()) {
			sb.append(it.next().toString()).append(", ");
		}
		sb.append("}");
		return sb.toString().replaceFirst(", }", "}");
	}

	/**
	 * 运算规则就是普通的数学表达式，运算优先级仅可用"()"指示，规则中可以包含具体的数值也可以包含一个或多个变量，
	 * 规则中仅支持三种类型的变量：String，Long和Date，对应这三类变量有如下强制规则</br> <li>
	 * String的表达式为：[#field_name#.hashCode()]</li> <li>
	 * Long的表达式为：[#field_name#.longValue()]</li> <li>
	 * Date的表达式为：[#field_name#.DAY_OF_WEEK]，其中DAY_OF_WEEK可以替换成任意</br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	 * java.util.Calendar类中支持java.util.Calendar.get(int field)方法的字段名</li>
	 * </br></br> 规则表达式示例：</br>
	 * ([#field_name1#.hashCode()]+[#field_name1#.longValue()])%4/2 </br></br>
	 * 
	 * @param rule
	 *            规则表达式
	 * @param fieldNames
	 *            excel中表示字段名的HSSFRow对象
	 * @param data
	 *            excel中的一行数据，data中的元素与fieldNames中的元素顺序一一对应
	 * @return 根据规则和变量值计算出的int值，如果实际计算出的值包含小数位，返回舍弃小数位后的int值
	 */
	private static int calRule(String rule, HSSFRow fieldNames, HSSFRow data) {
		String mathExp = replaceVariable(rule, fieldNames, data);
		Double result = (Double) calMathExp(mathExp);
		return result.intValue();
	}

	private static Object calMathExp(String expression) {
		Object result = null;
		try {
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine se = sem.getEngineByExtension("js");

			result = se.eval(expression);
		} catch (Exception e) {
			throw new RuntimeException("cal math expression failed: "
					+ expression);
		}
		return result;
	}

	private static String replaceVariable(String rule, HSSFRow fieldNames,
			HSSFRow data) {
		Map<String, String[]> variables = getVariableMap(rule);
		Map<String, Long> varValues = getVariableValue(variables, fieldNames,
				data);
		Set<String> keySet = varValues.keySet();
		for (String key : keySet) {
			String regex = key.replaceAll("[\\[\\]]", "");
			rule = rule.replaceAll(regex, varValues.get(key).toString());
		}
		rule = rule.replaceAll("[\\[\\]]", "").replaceAll("\\(\\)", "");
		return rule;
	}

	// return as: Map<"[#field_name#.hashCode()]", {"field_name",
	// ".hashCode()"}>
	private static Map<String, String[]> getVariableMap(String rule) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		String temp = new String(rule.replaceAll(" ", ""));
		while (temp.indexOf("[") != -1) {
			String key = temp.substring(temp.indexOf("["),
					temp.indexOf("]") + 1).trim();
			String fieldName = key
					.substring(key.indexOf("#") + 1, key.lastIndexOf("#"))
					.trim().toUpperCase();
			String operation = key
					.substring(key.indexOf("."), key.indexOf("]")).trim();
			String[] value = { fieldName, operation };
			map.put(key, value);

			temp = temp.replaceAll(key.substring(1, key.length() - 1), "")
					.replaceAll("\\[\\(\\)\\]", "").replaceAll("\\[\\]", "");
		}
		return map;
	}

	/**
	 * @param variables
	 *            : Map<"[#field_name#.hashCode()]", {"field_name",
	 *            ".hashCode()"}>
	 * @return Map<"[#field_name#.hashCode()]", 123>
	 */
	private static Map<String, Long> getVariableValue(
			Map<String, String[]> variables, HSSFRow fieldNames, HSSFRow data) {
		Map<String, Long> map = new HashMap<String, Long>();
		Map<String, Integer> fieldIndex = converFieldName2Map(fieldNames);
		Set<java.util.Map.Entry<String, String[]>> entries = variables
				.entrySet();
		for (java.util.Map.Entry<String, String[]> entry : entries) {
			String[] value = entry.getValue();
			String fieldName = value[0];
			String operation = value[1];
			Integer index = fieldIndex.get(fieldName);
			if (index == null) {
				throw new RuntimeException(
						"there has no Field named ["
								+ fieldName
								+ "] in the fields:\n "
								+ fieldIndex.keySet()
								+ ",\n please check your rule configuration or data preparation!");
			}
			if (LONE_VALUE.equals(operation)) {
				HSSFCell datacell = data.getCell(index);
				long rtValue = 0;
				if (datacell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
					rtValue = (long) datacell.getNumericCellValue();
				} else {
					rtValue = Long.valueOf(datacell.getRichStringCellValue()
							.getString().trim());
				}
				map.put(entry.getKey(), rtValue);
			} else if (HASH_CODE.equals(operation)) {
				long rtValue = data.getCell(index).getRichStringCellValue()
						.getString().hashCode();
				map.put(entry.getKey(), Math.abs(rtValue));
			} else {// can only be date operation
				Map<String, Date> replacements = getSysdateReplacement();
				Date date = replacements.get(data.getCell(index).toString());
				if (date == null) {
					date = data.getCell(index).getDateCellValue();
				}
				Calendar c = Calendar.getInstance();
				c.setTime(date);
				try {
					if (operation.startsWith(".")) {
						operation = operation.substring(1, operation.length());
					}
					Field cField = c.getClass().getField(operation);
					long rtValue = c.get(cField.getInt(cField));
					map.put(entry.getKey(), rtValue);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

		return map;
	}

	private static Map<String, Integer> converFieldName2Map(HSSFRow fieldNames) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		Iterator<Cell> it = fieldNames.cellIterator();
		Integer index = 0;
		while (it.hasNext()) {
			Cell cell = it.next();
			String cellValue = cell.getRichStringCellValue().getString().trim()
					.toUpperCase();
			map.put(cellValue, index);
			index++;
		}
		return map;
	}

	private static Map<String, Date> getSysdateReplacement() {
		Map<String, Date> replacement = new HashMap<String, Date>();
		replacement.put("sysdate", _now);
		long day = 3600 * 24 * 1000;
		for (int i = 1; i < 32; i++) {
			replacement.put("sysdate+" + i, new Date(_now.getTime() + i * day));
			replacement.put("sysdate-" + i, new Date(_now.getTime() - i * day));
		}
		return replacement;
	}

	private static void addRow2Workbook(HSSFWorkbook workbook,
			String sheetName, HSSFRow fieldNames, HSSFRow data) {
		HSSFSheet sheet = workbook.getSheet(sheetName);
		if (sheet == null) {
			sheet = workbook.createSheet(sheetName);
			addRow2Sheet(workbook, sheet, fieldNames);
		}
		addRow2Sheet(workbook, sheet, data);
	}

	private static void addRow2Sheet(HSSFWorkbook workbook, HSSFSheet sheet,
			HSSFRow row) {
		int lastRowNum = sheet.getLastRowNum();
		int newRowNum = lastRowNum + 1;
		if (lastRowNum == 0 && sheet.getPhysicalNumberOfRows() == 0) {
			newRowNum = 0;
		}
		HSSFRow targetRow = sheet.createRow(newRowNum);
		Iterator<Cell> it = row.cellIterator();
		while (it.hasNext()) {
			Cell sourceCell = it.next();
			HSSFCell targetCell = targetRow.createCell(sourceCell
					.getColumnIndex());
			// targetCell.setCellStyle(sourceCell.getCellStyle());
			if (sourceCell.getCellComment() != null) {
				targetCell.setCellComment(sourceCell.getCellComment());
			}
			int cType = sourceCell.getCellType();
			targetCell.setCellType(cType);
			switch (cType) {
			case HSSFCell.CELL_TYPE_BOOLEAN:
				targetCell.setCellValue(sourceCell.getBooleanCellValue());
				break;
			case HSSFCell.CELL_TYPE_ERROR:
				targetCell.setCellErrorValue(sourceCell.getErrorCellValue());
				break;
			case HSSFCell.CELL_TYPE_FORMULA:
				targetCell.setCellFormula(parseFormula(sourceCell
						.getCellFormula()));
				break;
			case HSSFCell.CELL_TYPE_NUMERIC:
				if (HSSFDateUtil.isCellDateFormatted(sourceCell)) {
					targetCell.setCellValue(sourceCell.getDateCellValue());
					HSSFCellStyle cellStyle = workbook.createCellStyle();
					HSSFDataFormat format = workbook.createDataFormat();
					cellStyle.setDataFormat(format
							.getFormat("yyyy-mm-dd hh:mm:ss"));
					targetCell.setCellStyle(cellStyle);
				} else {
					targetCell.setCellValue(sourceCell.getNumericCellValue());
					HSSFCellStyle cellStyle = workbook.createCellStyle();
					HSSFDataFormat format = workbook.createDataFormat();
					cellStyle.setDataFormat(format.getFormat(sourceCell
							.getCellStyle().getDataFormatString()));
					targetCell.setCellStyle(cellStyle);
				}
				break;
			case HSSFCell.CELL_TYPE_STRING:
				targetCell.setCellValue(sourceCell.getRichStringCellValue());
				break;
			}

		}
	}

	private static String parseFormula(String pPOIFormula) {
		final String cstReplaceString = "ATTR(semiVolatile)"; //$NON-NLS-1$
		StringBuffer result = null;
		int index;

		result = new StringBuffer();
		index = pPOIFormula.indexOf(cstReplaceString);
		if (index >= 0) {
			result.append(pPOIFormula.substring(0, index));
			result.append(pPOIFormula.substring(index
					+ cstReplaceString.length()));
		} else {
			result.append(pPOIFormula);
		}

		return result.toString();
	}

	public static void main(String[] args) {
		// System.out.println(((Double)calMathExp("7<<1")).intValue());
		// Calendar c = Calendar.getInstance();
		// Field field;
		// try {
		// field = c.getClass().getField("DAY_OF_WEEK");
		// System.out.println(field.getInt(field));
		// System.out.println(c.get(field.getInt(field)));
		// } catch (SecurityException e1) {
		// e1.printStackTrace();
		// } catch (NoSuchFieldException e1) {
		// e1.printStackTrace();
		// } catch (IllegalArgumentException e) {
		// e.printStackTrace();
		// } catch (IllegalAccessException e) {
		// e.printStackTrace();
		// }
		// HSSFWorkbook workbook = new HSSFWorkbook();
		// HSSFSheet sheet = workbook.createSheet();
		// HSSFRow row = sheet.createRow(0);
		// HSSFCell cell = row.createCell(0);
		// cell.setCellValue(new Date());
		// System.out.println(cell.getCellType());
		// System.out.println("cell="+cell.toString());
		// Iterator<Cell> it = row.cellIterator();
		// while(it.hasNext()){
		// System.out.println(it.next().toString());
		// }
		String key = "[#user_id#.DAYOFWEEK][]";
		// String rule = "[#user_id#.DAYOFWEEK]%3()-(3*2)";
		// String regex = key.replaceAll("[\\[\\]]", "");
		// System.out.println(regex);
		// rule = rule.replaceAll(regex, "333");
		// System.out.println(rule);
		// rule = rule.replaceAll("[\\[\\]]", "").replaceAll("\\(\\)",
		// "");//.replaceAll("[\\()]", "");
		// System.out.println(rule);
		System.out.println(key.substring(1, key.length() - 1));
		System.out.println(key.replaceAll("\\[\\]", ""));
	}
}
