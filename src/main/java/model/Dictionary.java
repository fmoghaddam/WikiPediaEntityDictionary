package model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
public class Dictionary {

	private static final Logger LOG = Logger.getLogger(Dictionary.class.getCanonicalName());
	private final ConcurrentHashMap<AnchorText, Set<MapEntity>> dic = new ConcurrentHashMap<>();
	private static final BiFunction<Set<MapEntity>, Set<MapEntity>, Set<MapEntity>> biFunction = (oldSet, entity) -> {
		try {
			final Entity newEntity = entity.stream().findFirst().get().getEntity();
			Stream<MapEntity> filter = oldSet.stream().filter(p -> p.getEntity().getUri().equals(newEntity.getUri()));
			if (filter.count() != 0) {
				Optional<MapEntity> findFirst = oldSet.stream()
						.filter(p -> p.getEntity().getUri().equals(newEntity.getUri())).findFirst();
				findFirst.get().increment();
			} else {
				oldSet.add(new MapEntity(newEntity));
			}
		} catch (Exception exception) {
			LOG.error(exception);
		}
		return oldSet;

	};

	public void merge(final AnchorText anchorText, final Entity value) {
		if (anchorText == null || value == null) {
			throw new IllegalArgumentException();
		}
		final Set<MapEntity> set = new HashSet<>();
		final MapEntity mapEntity = new MapEntity(value);
		set.add(mapEntity);
		dic.merge(anchorText, set, biFunction);
		dic.keySet().stream().filter(p -> p.equals(anchorText)).findFirst().get().increment();

	}

	public void printResult() {
		for (final Entry<AnchorText, Set<MapEntity>> entry : dic.entrySet()) {
			StringBuilder result = new StringBuilder();
			result.append(entry.getKey().getAnchorText()).append(";").append(entry.getKey().getFrequency()).append(";");
			result.append("size").append(";").append(entry.getValue().size()).append(";");
			for (MapEntity mapEntity : entry.getValue()) {
				result.append(mapEntity.getEntity().getEntityName()).append(";").append(mapEntity.getFrequency())
				.append(";");
			}
			LOG.info(result.toString());
		}

	}

	public void printToXLS(){
		final Workbook wb = new HSSFWorkbook();
		final Sheet sheet = wb.createSheet("new sheet");
		int startRow = 0;
		int endRow = 0;
		int columnNumber = 0;
		int rowNumber = 0;
		for (final Entry<AnchorText, Set<MapEntity>> entry : dic.entrySet()) {			
			final Row row = sheet.createRow((short) rowNumber);
			Cell cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getKey().getAnchorText());

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getKey().getFrequency());

			cell = row.createCell((short) columnNumber++);
			cell.setCellValue(entry.getValue().size());

			int innerRowNumer = new Integer(rowNumber).intValue()+1;
			startRow = innerRowNumer-1;
			endRow = startRow;

			boolean firstTime = true;
			for (final MapEntity mapEntity : entry.getValue()) {
				if(firstTime){
					row.createCell((short) columnNumber).setCellValue(mapEntity.getEntity().getEntityName());
					row.createCell((short) columnNumber+1).setCellValue(mapEntity.getFrequency());
					row.createCell((short) columnNumber+2).setCellValue(mapEntity.getEntity().getCategoryFolder());
					firstTime = false;
				}else{
					int innerColumnNumber = new Integer(columnNumber).intValue();
					final Row innerRow = sheet.createRow((short) innerRowNumer++);
					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getEntity().getEntityName());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getFrequency());

					cell = innerRow.createCell((short) innerColumnNumber++);
					cell.setCellValue(mapEntity.getEntity().getCategoryFolder());
					endRow++;
				}
			}	
			if(startRow<endRow){
				sheet.addMergedRegion(new CellRangeAddress(
						startRow, //first row (0-based)
						endRow, //last row  (0-based)
						0, //first column (0-based)
						0  //last column  (0-based)
						));
				sheet.addMergedRegion(new CellRangeAddress(
						startRow, //first row (0-based)
						endRow, //last row  (0-based)
						1, //first column (0-based)
						1  //last column  (0-based)
						));
				sheet.addMergedRegion(new CellRangeAddress(
						startRow, //first row (0-based)
						endRow, //last row  (0-based)
						2, //first column (0-based)
						2  //last column  (0-based)
						));
			}
			rowNumber=endRow+1;
			startRow=endRow+1;
			columnNumber=0;
		}


		// Write the output to a file
		final FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream("workbook.xls");
			wb.write(fileOut);
			fileOut.close();
			wb.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void printResultLineByLine() {
		for (final Entry<AnchorText, Set<MapEntity>> entry : dic.entrySet()) {
			boolean firstline = true;
			StringBuilder result = new StringBuilder();			
			for (MapEntity mapEntity : entry.getValue()) {
				if(firstline){
					result.append(entry.getKey().getAnchorText()).append(";").append(entry.getKey().getFrequency()).append(";").append(entry.getValue().size()).append(";");
					firstline=false;
				}else{
					result.append(";;;");
				}
				// result.append(URLUTF8Encoder.unescape(mapEntity.getEntity().getUri())).append(";").append(mapEntity.getFrequency());
				result.append(mapEntity.getEntity().getEntityName()).append(";").append(mapEntity.getFrequency())
				.append(";");
				result.append(mapEntity.getEntity().getCategoryFolder());
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
