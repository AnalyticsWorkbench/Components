package eu.sisob.components.relationalextractor.connector;


import java.sql.Connection;
import java.sql.DriverManager;

public class DataBaseConnector {

	private String user;
	private String password;
	private String db_url;
	
	private Connection conn;    
    private java.sql.PreparedStatement stmt;
    private java.sql.ResultSet rset;
	
	
	public DataBaseConnector (String user, String password, String db_URL){		
		this.user=user;
		this.password=password;
		this.db_url=db_URL;		
	}
	
	public void connectToDB()throws Exception{
		  Class.forName("com.mysql.jdbc.Driver").newInstance();
          conn = DriverManager.getConnection(db_url, user, password); 
          if(conn!=null)
        	  System.out.println("Connection accepted!");          
	}
	
	public void executeQuery(String query)throws Exception{
		 stmt = conn.prepareStatement(query);
         rset = stmt.executeQuery();
	}
	
	public void disconnectoFromDB()throws Exception{
		if(rset != null)
            rset.close();
        conn.close();  
	}		
	
	public String getResults()throws Exception{
	StringBuilder result = new StringBuilder();
	int column_count = rset.getMetaData().getColumnCount();	
		while (rset.next()) {
			for(int i=1;i<=column_count;i++){				
				result.append(rset.getString(i));
				if(i!=column_count)
				result.append("@@@");	
			}			 
	      result.append(System.getProperty("line.separator"));			
		} 		
		return result.toString();
	}
	
	public void printResults(String results){
		System.out.println(results);
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDb_url() {
		return db_url;
	}

	public void setDb_url(String db_url) {
		this.db_url = db_url;
	}

	public Connection getConn() {
		return conn;
	}

	public void setConn(Connection conn) {
		this.conn = conn;
	}

	public java.sql.PreparedStatement getStmt() {
		return stmt;
	}

	public void setStmt(java.sql.PreparedStatement stmt) {
		this.stmt = stmt;
	}

	public java.sql.ResultSet getRset() {
		return rset;
	}

	public void setRset(java.sql.ResultSet rset) {
		this.rset = rset;
	}
	
}
