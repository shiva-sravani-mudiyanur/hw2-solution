import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import controller.ExpenseTrackerController;
import model.ExpenseTrackerModel;
import model.Transaction;
import view.ExpenseTrackerView;

public class TestExportToCSV {

    private ExpenseTrackerModel expenseTrackerModelMock;
    private MockView viewMock;
    private ExpenseTrackerController expenseTrackerControllerMock;

    public static class MockView extends ExpenseTrackerView {
        private String message;
        @Override
        public void displayExportStatusMessage(String message) {
            this.message = message;
        }
        public String getLastMessage() {
            return message;
        }
    }

    @Before
    public void setup() {
    	expenseTrackerModelMock = new ExpenseTrackerModel();
    	viewMock = new MockView();
    	expenseTrackerControllerMock = new ExpenseTrackerController(expenseTrackerModelMock, viewMock);
    }


    @Test
    public void testValidCsvFilename() {
    	
        assertTrue(expenseTrackerControllerMock.isValidCsvFilename("csvFile.csv"));
        assertTrue(expenseTrackerControllerMock.isValidCsvFilename("csv_file.csv"));
        assertTrue(expenseTrackerControllerMock.isValidCsvFilename("csv file with space.csv"));
    }
    
    @Test
    public void testInValidCsvFilename() {
      
        assertEquals(false, expenseTrackerControllerMock.isValidCsvFilename(""));
        assertEquals(false, expenseTrackerControllerMock.isValidCsvFilename("FileNameWithoutExtention"));
        assertEquals(false, expenseTrackerControllerMock.isValidCsvFilename("....Wrong$NamingConvention.csv"));
        
    }
    
    public void createMockCsvFile()
    {
    	expenseTrackerModelMock.addTransaction(new Transaction(23.76, "food"));
    	expenseTrackerModelMock.addTransaction(new Transaction(63.89, "travel"));
    	expenseTrackerModelMock.addTransaction(new Transaction(63.89, "bills"));
    	expenseTrackerModelMock.addTransaction(new Transaction(63.89, "entertainment"));
    	expenseTrackerModelMock.addTransaction(new Transaction(63.89, "other"));

    	expenseTrackerControllerMock.exportTransactionsListToCsv("mockCSVFile.csv");
    }

    @Test
    public void testIfHeaderExists() throws IOException {
    	
    	createMockCsvFile();

        File csvFile = new File("mockCSVFile.csv");
        List<String> transactions = Files.readAllLines(csvFile.toPath());
        assertEquals("Date,Amount,Category", transactions.get(0));
     
    }
    
    @Test
    public void testNoOfLinesInTheFile() throws IOException {
    	
    	createMockCsvFile();

        File csvFile = new File("test-output.csv");
        assertTrue(csvFile.exists());
        List<String> transactions = Files.readAllLines(csvFile.toPath());
        assertTrue(transactions.size() >= 6);
    }

    @Test
    public void testContentsOfTheFile() throws IOException {
    	
    	createMockCsvFile();
        File csvFile = new File("mockCSVFile.csv");        
        List<String> transactions = Files.readAllLines(csvFile.toPath());     
        assertTrue(transactions.get(1).contains("food"));
        assertTrue(transactions.get(2).contains(",63.89,"));
    }

    @Test
    public void testIfDisplaysInvalidMessageForInvalidFileNames() {
    	
    	expenseTrackerControllerMock.exportTransactionsListToCsv("FileNameWithoutExtention");
        assertEquals("Invalid file name. Please provide a valid .csv name.", viewMock.getLastMessage());

        expenseTrackerControllerMock.exportTransactionsListToCsv("$Wrong$Naming$Convention$.csv");
        assertEquals("Invalid file name. Please provide a valid .csv name.", viewMock.getLastMessage());
        
        expenseTrackerControllerMock.exportTransactionsListToCsv("");
        assertEquals("Invalid file name. Please provide a valid .csv name.", viewMock.getLastMessage());

    }

    
    @Test
    public void testExport_ioException_displaysError() throws IOException {
        // Create a directory named ioerror.csv so opening as a file throws IOException
        Files.createDirectories(Paths.get("ioerror.csv"));
        expenseTrackerControllerMock.exportTransactionsListToCsv("ioerror.csv");
        String msg = viewMock.getLastMessage();
        assertTrue(msg.startsWith("CSV File Export Failed"));
    }
    
}
