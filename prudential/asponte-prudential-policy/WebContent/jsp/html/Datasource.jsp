<%@ page import="java.util.logging.Level"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page import="javax.naming.Context"%>
<%@ page import="javax.naming.InitialContext"%>
<%@ page import="javax.naming.NamingException"%>
<%@ page import="javax.sql.DataSource"%>
<%@ page import="java.sql.Connection"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.SQLException"%>

<%!

/** Logger for the class */
   private static Logger s_log = Logger.getLogger("Datasource.jsp");
   
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

%>

<%
	DataSource ds = getDataSource();
	out.println(ds);
%>