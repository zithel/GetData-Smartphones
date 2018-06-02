package application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import javax.swing.JOptionPane;
// Third-party
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
// Custom
import resource.*;

public class GetDataGSMArena
{
	public static void main(String[] args) throws Exception
	{
		final String idPrdct = "HkuGfJg";
		
		int tmpInt1, tmpInt2;
		Connection sqlCnnct;
		Document objHTMLMn, objHTMLSb;
		LocalDateTime tmStrtMn;
		Statement sqlStmnt1, sqlStmnt2;
		String strBrnd, strURL;
		FAZTools FZTls = new FAZTools();
		UniqueRandomId idUnqPrdct = new UniqueRandomId(10);
		UniqueRandomId idUnqRn = new UniqueRandomId(15);
		
		try
		{
		// *** PRELIMINARY SETUP ***
			tmStrtMn = LocalDateTime.now();

			Class.forName("com.mysql.jdbc.Driver");
			sqlCnnct = DriverManager.getConnection("jdbc:mysql://localhost:3306/zithelMaster","fjawed","password");
			sqlStmnt1 = sqlCnnct.createStatement();
			sqlStmnt2 = sqlCnnct.createStatement();

			// Construct DB structure
			sqlStmnt1.execute("use zithelMaster;");
			sqlStmnt1.execute("create table if not exists mstr_Products (ID varchar(10) primary key, BRAND_NAME text not null, NAME text not null, PRODUCT_TYPE varchar(10) not null, PAGE_URL text, IMAGE_URL text, IS_ACTIVE bit not null, PROCESSED_IN varchar(10));");
			sqlStmnt1.execute("delete from mstr_Products where PRODUCT_TYPE = '" + idPrdct + "';");
		
	    	try
	    	{
	    		objHTMLMn = Jsoup.connect("https://www.gsmarena.com/makers.php3").timeout(10000).get();
	    	}
	    	catch (Exception e)
	    	{
				objHTMLMn = Jsoup.connect("https://www.gsmarena.com/makers.php3").timeout(20000).get();
	    	}
		    for(Element lpRw : objHTMLMn.select("div.st-text").select("tr"))
		    {
		        for(Element lpCll : lpRw.select("td"))
		        {
		            strBrnd = lpCll.text().split(" ")[0].replace("'", "''");
		            strURL = "https://www.gsmarena.com/" + lpCll.select("a").attr("href").replace("about:", "");
		            tmpInt1 = 1;
		            do
		            {
		            	try
		            	{
		            		objHTMLSb = Jsoup.connect(strURL).timeout(6000).get();
		            	}
		            	catch (Exception e)
		            	{
		            		objHTMLSb = Jsoup.connect(strURL).timeout(10000).get();
		            	}
		
		            //Get Phone info
		                for(Element lpElmnt : objHTMLSb.select("#review-body").select("li"))
		                	sqlStmnt2.execute("INSERT INTO mstr_Products (ID,BRAND_NAME,NAME,PRODUCT_TYPE,PAGE_URL,IMAGE_URL,IS_ACTIVE) VALUES ('" + idUnqPrdct.Generate() + "','" + strBrnd + "','" + lpElmnt.text().replace("'", "''") + "','" + idPrdct + "','http://www.gsmarena.com/" + lpElmnt.select("a").attr("href").replace("about:", "") + "','" + lpElmnt.select("img").attr("src") + "',1)");
		                	
		            //Get Next page info
		                if(!objHTMLSb.select("div.nav-pages").isEmpty())
		                {
	 	                    tmpInt1++;
		                    for(Element lpElmnt : objHTMLSb.selectFirst("div.nav-pages").children())
		                    {
		                    	try
		                    	{
			                        if(Integer.valueOf(lpElmnt.text()) == tmpInt1)
			                        {
			                            strURL = "http://www.gsmarena.com/" + lpElmnt.attr("href").replace("about:", "");
			                            break;
			                        }
		                    	}
		                    	catch (Exception e)
		                    	{
		                    		continue;
		                    	}
		                    }
		                    tmpInt2 = objHTMLSb.selectFirst("div.nav-pages").children().size() - 1;
		                }
		                else
		                    tmpInt2 = 0;
		            }
		            while(tmpInt1 <= tmpInt2);
				}
			}
			sqlStmnt2.execute("INSERT INTO mstr_script_run (ID,SCRIPT_NAME,START_TIME,COMPLETED_IN) VALUES('" + idUnqRn.Generate()+ "','MBL-GSMARENA','" + tmStrtMn.toString() + "','" + FZTls.TimeString(tmStrtMn.until(LocalDateTime.now(), ChronoUnit.SECONDS)) + "');");
		    sqlCnnct.close();
	        JOptionPane.showMessageDialog(null, "Data collation completed in " + FZTls.TimeString(tmStrtMn.until(LocalDateTime.now(), ChronoUnit.SECONDS)), "Zithel data assistant", JOptionPane.INFORMATION_MESSAGE);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}