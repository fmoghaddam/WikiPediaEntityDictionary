package model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class Dictionary {

	private static final Logger LOG = Logger.getLogger(Dictionary.class.getCanonicalName());
	private final ConcurrentHashMap<AnchorText, Map<String, MapEntity>> dic = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<AnchorText, Long> dicKeyFrequency = new ConcurrentHashMap<>();

	public void merge(final AnchorText anchorText, final Entity entity) {
		if (anchorText == null || entity == null) {
			throw new IllegalArgumentException();
		}

		final MapEntity mapEntity = new MapEntity(entity);

		Map<String, MapEntity> dicElement = dic.get(anchorText);
		if (dicElement == null) {
			final Map<String, MapEntity> map = new ConcurrentHashMap<>();
			map.put(mapEntity.getEntity().getUri(), mapEntity);
			dic.put(anchorText, map);
			dicKeyFrequency.put(anchorText, anchorText.getFrequency());
		} else {
			MapEntity mapElement = dicElement.get(entity.getUri());
			if (mapElement == null) {
				dicElement.put(entity.getUri(), mapEntity);
			} else {
				mapElement.increment();
			}
		}

		dicKeyFrequency.put(anchorText,
				dicKeyFrequency.containsKey(anchorText) ? (dicKeyFrequency.get(anchorText).longValue() + 1) : 1);

	}

	public void printResult() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dic.entrySet()) {
			StringBuilder result = new StringBuilder();
			result.append(entry.getKey().getAnchorText()).append(";").append(dicKeyFrequency.get(entry.getKey()))
					.append(";");
			result.append(entry.getValue().size()).append(";");
			for (MapEntity mapEntity : entry.getValue().values()) {
				result.append(mapEntity.getEntity().getEntityName()).append(";").append(mapEntity.getFrequency())
						.append(";");
			}
			LOG.info(result.toString());
		}
	}

	public void printToXLS() {
		final Workbook wb = new HSSFWorkbook();
		final Sheet sheet = wb.createSheet("new sheet");
		int startRow = 0;
		int endRow = 0;
		int columnNumber = 0;
		int rowNumber = 0;
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dic.entrySet()) {
			final Row row = sheet.createRow((short) rowNumber);
			Cell cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getKey().getAnchorText());

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(dicKeyFrequency.get(entry.getKey()));

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getValue().size());

			int innerRowNumer = new Integer(rowNumber).intValue() + 1;
			startRow = innerRowNumer - 1;
			endRow = startRow;

			boolean firstTime = true;
			for (final Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				if (firstTime) {
					row.createCell((short) columnNumber).setCellValue(mapEntity.getValue().getEntity().getEntityName());
					row.createCell((short) columnNumber + 1).setCellValue(mapEntity.getValue().getFrequency());
					row.createCell((short) columnNumber + 2)
							.setCellValue(mapEntity.getValue().getEntity().getCategoryFolder());
					firstTime = false;
				} else {
					int innerColumnNumber = new Integer(columnNumber).intValue();
					final Row innerRow = sheet.createRow((short) innerRowNumer++);
					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getEntity().getEntityName());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getFrequency());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getValue().getEntity().getCategoryFolder());
					endRow++;
				}
			}
			if (startRow < endRow) {
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 0, 0));
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 1, 1));
				sheet.addMergedRegion(new CellRangeAddress(startRow, endRow, 2, 2));
			}
			rowNumber = endRow + 1;
			startRow = endRow + 1;
			columnNumber = 0;
		}

		// Write the output to a file
		final FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("workbook.xls");
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printResultLineByLineByMerge() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dic.entrySet()) {
			boolean firstline = true;
			StringBuilder result = new StringBuilder();
			for (Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				if (firstline) {
					result.append(entry.getKey().getAnchorText()).append(";")
							.append(dicKeyFrequency.get(entry.getKey())).append(";").append(entry.getValue().size())
							.append(";");
					firstline = false;
				} else {
					result.append(";;;");
				}
				// result.append(URLUTF8Encoder.unescape(mapEntity.getEntity().getUri())).append(";").append(mapEntity.getFrequency());
				result.append(mapEntity.getValue().getEntity().getEntityName()).append(";")
						.append(mapEntity.getValue().getFrequency()).append(";");
				result.append(mapEntity.getValue().getEntity().getCategoryFolder());
				LOG.info(result.toString());
				result = new StringBuilder();
			}
		}
	}

	public void printResultLineByLine() {
		for (final Entry<AnchorText, Map<String, MapEntity>> entry : dic.entrySet()) {
			StringBuilder result = new StringBuilder();
			for (Entry<String, MapEntity> mapEntity : entry.getValue().entrySet()) {
				result.append(entry.getKey().getAnchorText()).append(";").append(dicKeyFrequency.get(entry.getKey()))
						.append(";").append(entry.getValue().size()).append(";");
				result.append(mapEntity.getValue().getEntity().getUri()).append(";")
						.append(mapEntity.getValue().getFrequency()).append(";");
				LOG.info(result.toString());
				result = new StringBuilder();
			}
		}
	}

	@Override
	public String toString() {
		return "Dictionary [dic=" + dic + "]";
	}

	public int size() {
		return dic.size();
	}
}
