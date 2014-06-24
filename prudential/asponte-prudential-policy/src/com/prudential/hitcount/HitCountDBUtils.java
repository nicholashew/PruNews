/********************************************************************/
/* Asponte
/* cmknight
/********************************************************************/

package com.prudential.hitcount;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
public class HitCountDBUtils {

   /** Logger for the class */
   private static Logger s_log = Logger.getLogger(HitCountDBUtils.class.getName());
   
   protected static final String INSERT_SQL = "INSERT INTO HitCount ( hit_uuid, hit_count) VALUES ( ?,  1) ";
   protected static final String SELECT_SQL = "select hit_count from HitCount where hit_uuid = ?";
   protected static final String SELECT_COUNT_SQL = "SELECT COUNT(*) FROM HitCount where hit_uuid = ?";
   protected static final String UPDATE_SQL = "UPDATE HitCount SET hit_count = ? where hit_uuid = ?";
   private static final String JDBC_NCDS = "jdbc/apps";
   private static final String JDBC_LOOKUP_ENV = "java:comp/env";
   
   
   /**
    * 
    * getConnection helper method to get the connection
    * @return connection object
    */
   public static Connection getConnection() {
      Context ctx;
      Connection conn = null;
      try {
         ctx = new InitialContext();
         DataSource dataSource = getDataSource();
         conn = dataSource.getConnection();
      }
      catch (NamingException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      catch (SQLException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      
      
      return conn;
   }
   
   /**
    * 
    * getDataSource helper method to retrieve a datasource
    * @return
    */
   public static DataSource getDataSource() {
      DataSource dataSource = null;
      Context ctx;
      try {
         ctx = new InitialContext();
         //ctx =(Context) new InitialContext().lookup(JDBC_LOOKUP_ENV);
         dataSource = (DataSource) ctx.lookup(JDBC_NCDS);        
      }
      catch (NamingException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
            e.printStackTrace();
         }
      }
      
      return dataSource;
   }
   
   /**
    * 
    * updateCount description
    * @param uuid
    */
   public static void updateCount(String uuid) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("HitCountDBUtils", "updateCount "+uuid);
      }
      PreparedStatement pstmt = null;
      Connection conn = getConnection();
      ResultSet rs = null;
      try {
         pstmt = conn.prepareStatement(SELECT_SQL);
         pstmt.setString(1, uuid);
         rs = pstmt.executeQuery();
         // if I have a result, means I can do the insert
         if(rs.next()) {            
            int count = rs.getInt(1);
            if (isDebug) {
               s_log.log(Level.FINEST, "count wasn't empty, doing update, count is "+count);
            }
            count++;
            rs.close();
            pstmt.clearBatch();
            pstmt = conn.prepareStatement(UPDATE_SQL);
            pstmt.setInt(1,count);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
            pstmt.clearBatch();
         }
         // otherwise, do an update
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "count was empty, doing insert");
            }
            pstmt.clearBatch();
            pstmt = conn.prepareStatement(INSERT_SQL);
            pstmt.setString(1, uuid);
            pstmt.executeUpdate();
            pstmt.clearBatch();
         }
         
      }
      catch (SQLException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
         }
      }
      finally {
         if(conn != null) {
            try {
               conn.close();
            }
            catch (SQLException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST))
               {
                  s_log.log(Level.FINEST, "", e);
               }
            }
         }
         if(rs != null) {
            try {
               rs.close();
            }
            catch (SQLException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST))
               {
                  s_log.log(Level.FINEST, "", e);
               }
            }
         }
      }
      
      if (isDebug) {
         s_log.exiting("HitCountDBUtils", "updateCount");
      }
      
      
   }
   
   /**
    * 
    * getCount description
    * @param uuid
    * @return
    */
   public static int getCount(String uuid) {
      boolean isDebug = s_log.isLoggable(Level.FINEST);
      if (isDebug) {
         s_log.entering("HitCountDBUtils", "getCount "+uuid);
      }
            
      int count = 0;      
      PreparedStatement pstmt = null;
      Connection conn = getConnection();
      ResultSet rs = null;
      try {
         pstmt = conn.prepareStatement(SELECT_SQL);
         pstmt.setString(1, uuid);
         rs = pstmt.executeQuery();
         // if I have a result, means I can do the insert
         if(rs.next()) {            
            count = rs.getInt(1);
            if (isDebug) {
               s_log.log(Level.FINEST, "count retrieved from DB "+count);
            }
         }
         else {
            if (isDebug) {
               s_log.log(Level.FINEST, "count not retrieved from DB "+count);
            }
         }
      }
      catch (SQLException e) {
         // TODO Auto-generated catch block
         if (s_log.isLoggable(Level.FINEST))
         {
            s_log.log(Level.FINEST, "", e);
            e.printStackTrace();
         }
      }
      finally {
         if(conn != null) {
            try {
               conn.close();
            }
            catch (SQLException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST))
               {
                  s_log.log(Level.FINEST, "", e);
                  e.printStackTrace();
               }
            }
         }
         if(rs != null) {
            try {
               rs.close();
            }
            catch (SQLException e) {
               // TODO Auto-generated catch block
               if (s_log.isLoggable(Level.FINEST))
               {
                  s_log.log(Level.FINEST, "", e);
                  e.printStackTrace();
               }
            }
         }
      }
      
      if (isDebug) {
         s_log.exiting("HitCountDBUtils", "getCount returning "+count);
      }
      
      return count;
   }
}

