package jeongsik;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/*
 * 매장을 선택하는 클래스
 */
public class SelectStore {

   // Oracle 데이터 베이스 연결 정보
   private static final String DB_URL = "jdbc:oracle:thin:@192.168.0.33:1521/XE"; // XE 대신에 SERVICE_NAME 사용
   private static final String USER = "C##SALMON"; // 스키마명 추가
   private static final String PASSWORD = "1234";//데이터베이스 비밀번호
   private static final Logger logger = Logger.getLogger(DB_URL);
   
   private StoreMenu selectedStore; // 선택된 매장 정보를 저장하는 변수
   
   public void displayStoreList() {
      // 1. 지역 목록 출력
      List<String> districtList = getDistrictList();
      int uerDistrictChoice = getUserDistrictChoice(districtList);
      
      if(uerDistrictChoice != -1) {
         String selectedDistrict = districtList.get(uerDistrictChoice -1   );
         //2. 선택된 지역의 매장 목록 가져오기
         List<StoreMenu> storeList = getStoreList(selectedDistrict);
         
         if(storeList.size() > 0   ) {
            System.out.println("가용한 매장 목록: ");
            for(int i = 0;i<storeList.size();i++) {
               System.out.println((i+1) + ". " + storeList.get(i).getStoreName());
            }
            
            // 3. 사용자에게 매장 선택 입력 받기
            int userChoice = getUserChoice(storeList.size());
            
            if(userChoice != -1) {
               //4. 사용자가 선택한 매장 정보출력
               selectedStore = storeList.get(userChoice-1);
               System.out.println("선택한 매장: " + selectedStore.getStoreName() + ", 위치: " + selectedStore.getLocation()   );
               
            }else {
               System.out.println("잘못된 선택입니다.");
            }
         }else {
            System.out.println("선택한 지역에 가용한 매장이 없습니다.");
         }
      }else {
         System.out.println("가용한 지역이 없습니다.");
      }
   }
   
   // 1. 지역 목록을 가져오는 메서드
   private List<String> getDistrictList(){
      List<String> districtList = new ArrayList<>();
      
      Connection connection = null;
      PreparedStatement statement = null;
      ResultSet resultSet = null;
      
      try {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
         
         //1-1 SQL 쿼리 수정 - DISTRICT 컬럼을 가져오도록 수정
         String query = "SELECT DISTINCT DISTRICT FROM STOREMENU";
         statement = connection.prepareStatement(query);
         resultSet = statement.executeQuery();
         
         while(resultSet.next()) {
            String district = resultSet.getString("DISTRICT");
            districtList.add(district);
         }
      }catch(ClassNotFoundException | SQLException e) {
         e.printStackTrace();
      }finally {
         // 리소스 래제
         closeResources(resultSet, statement, connection);
      }
      return districtList;
   }
   
   //2. 사용자에게 지역 선택 입력 받기
   private int getUserDistrictChoice(List<String> districtList) {
      System.out.println("가용한 지역 목록: ");
      for(int i=0;i<districtList.size();i++) {
         System.out.println((i+1) + ". " + districtList.get(i));
         
      }
      System.out.print("원하는 지역을 선택하세요 (1-" + districtList.size()+"): ");
      Scanner scanner = new Scanner(System.in);
      
      try {
         int choice = scanner.nextInt();
         if(choice >= 1 && choice <= districtList.size()) {
            return choice;
         }
      }catch (InputMismatchException e) {
         // 정수가 아닌 입력을 처리
         System.out.println("숫자를 입력하세요.");
      }
      return -1; // 잘못된 선택이나 입력이면 -1 반환
   }
   //2. 선택된 지역의 매장 목록을 가져오는 메서드
   private List<StoreMenu> getStoreList(String selectedDistrict){
      List<StoreMenu> storeList = new ArrayList<>();
      
      Connection connection =  null;
      PreparedStatement statement = null;
      ResultSet resultSet = null;
      
      try {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
         
         // 2-1. SQL 쿼리 수정 - 선택된 지역의 매장 목록을 가져오도록 수정
         String query = "SELECT STORENAME, LOCATION FROM STOREMENU WHERE DISTRICT = ?   ";
         statement = connection.prepareStatement(query);
         statement.setString(1, selectedDistrict);
         resultSet = statement.executeQuery();
         
         while(resultSet.next()) {
            String storeName = resultSet.getString("STORENAME");
            String location = resultSet.getString("LOCATION");
            // 기타 필요한 필드 추가
            
            StoreMenu storeMenu = new StoreMenu(storeName, location);
            storeList.add(storeMenu);
         }
      }catch(ClassNotFoundException | SQLException e) {
         e.printStackTrace();
      }finally {
         // 리소스 해제
         closeResources(resultSet, statement, connection);
      }
      return storeList;
   }
   
   
   //나머지 메서드 (커뮤니케리션 등 추가)
   public void communicateWithServer() {
      // 서버와 통신하는 코드 작성
   }
   // 리소스 해제 메서드
   private static void closeResources(ResultSet resultSet, PreparedStatement statement,Connection connection ) {
      try {
         if(resultSet != null)
            resultSet.close();
         if(statement!= null)
            statement.close();
         if(connection != null)
            connection.close();
      }catch(SQLException e) {
         
      }
   }
   private int getUserChoice(int maxChoice) {
      System.out.print("원하는 매장을 선택하세요 (1-" + maxChoice + "): ");
      Scanner scanner = new Scanner(System.in);
      
      try {
         int choice = scanner.nextInt();
         if(choice >= 1 && choice <= maxChoice) {
            return choice;
         }
      }catch(InputMismatchException e) {
         // 정수가 아닌 입력을 처리
         System.out.println("숫자를 입력하세요.");
      }
      
      
      return -1; // 잘못된 선택이나 입력이면 -1반환
   }
   
   // StoreMenu 클래스 정의
   public class StoreMenu{
      private String storeName;
      private String location;
      
      public StoreMenu(String storeName, String location) {
         this.storeName = storeName;
         this.location = location;
         
      }
      public String getStoreName() {
         return storeName;
      }
      public String getLocation() {
         return location;
      }
   }
   // 선택된 매장 정보를 반환하는 메서드
   public StoreMenu getSelectedStore() {
      return selectedStore;
   }
}