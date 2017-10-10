package dk.alexandra.fresco.framework.sce.resources.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import dk.alexandra.fresco.framework.sce.resources.storage.exceptions.NoMoreElementsException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Test;

public class TestStorage {

  @Test
  public void testStorageStrategy() {
    for(StorageStrategy s : StorageStrategy.values()) {
      Storage store = StorageStrategy.fromEnum(s);
      assertNotNull(store);
    }
    Storage storage = StorageStrategy.fromString("IN_MEMORY");
    assertNotNull(storage);
    storage = StorageStrategy.fromString("STREAMED_STORAGE");
    assertNotNull(storage);
    storage = StorageStrategy.fromString("Nonsense");
    assertNull(storage);
  }
  
  @Test
  public void testInMemoryStorage() {
    Storage storage = StorageStrategy.fromEnum(StorageStrategy.IN_MEMORY);
    testStorage(storage);
  }
  
  @Test
  public void testFileBasedStorage() {
    StreamedStorage storage = (StreamedStorage) StorageStrategy.fromEnum(StorageStrategy.STREAMED_STORAGE);
    testStorage(storage);
    testStreamedStorage(storage);
  }
  
  public void testStorage(Storage storage) {
    String testString = "This is a test";
    String storageName = "testStore";
    String keyName = "testKey";
    storage.putObject(storageName, keyName, testString);
    String o = storage.getObject(storageName, keyName);
    assertEquals(testString, o);
    
    String incorrect_name = "testStore1";
    o = storage.getObject(incorrect_name, keyName);
    assertNull(o);
    
    String incorrect_key = "testKey1";
    o = storage.getObject(storageName, incorrect_key);
    assertNull(o);
  }
  
  public void testStreamedStorage(StreamedStorage storage) {
    String filename = "testFile";
    String testString1 = "This is a test";
    String testString2 = "This is a second test";
    storage.putNext(filename, testString1);
    storage.putNext(filename, testString2);
    try {
      String s = storage.getNext(filename);    
      assertEquals(testString1, s);
      s = storage.getNext(filename);
      assertEquals(testString2, s);
    } catch(NoMoreElementsException e) {
      fail("Elements should exist in the store");
    } catch (FileNotFoundException e) {
      fail("File should be present");
    }
    try {
      storage.getNext(filename);
      fail("Should have cast a NoMoreElementsException");
    } catch(NoMoreElementsException e) {
      //Supposed to happen, so we're happy
    } catch (FileNotFoundException e) {
      fail("This is not the exception you are looking for");
    }
    try {
      storage.deleteStore(filename);
    } catch (IOException e) {
      fail("Should be able to delete store");
    }
    
    try {
      storage.deleteStore(filename);
      fail("Should not be able to delete store as we just did this");
    } catch (IOException e) {      
    }
    
    try {
      storage.getNext("FAIL");
      fail("Should not be able to get anything");
    } catch (NoMoreElementsException e) {
      fail("Filename should not exist");
    } catch (FileNotFoundException ex) {
      //Good - we expect this
    }
    
    storage.shutdown();
  }
}
