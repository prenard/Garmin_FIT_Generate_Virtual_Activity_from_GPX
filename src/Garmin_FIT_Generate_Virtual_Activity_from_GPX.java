/*

https://openclassrooms.com/fr/courses/2654406-java-et-le-xml

*/

import java.util.*;
import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import java.lang.Math;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.garmin.fit.*;

public class Garmin_FIT_Generate_Virtual_Activity_from_GPX
{
	   public static void main(String[] args)
	   {
		      // Prints "Hello, World" in the terminal window.
		      System.out.println("Hello, World");

		      System.out.printf("FIT Encode Example Application - Protocol %d.%d Profile %.2f %s\n",
                        Fit.PROTOCOL_VERSION_MAJOR,
                        Fit.PROTOCOL_VERSION_MINOR,
                        Fit.PROFILE_VERSION / 100.0,
                        Fit.PROFILE_TYPE);

		      FileEncoder fit_file_encode;

		      //String gpx_filename = "Walk_-_Asfnee.gpx";
		      //String gpx_filename = "Tour_Grand_Ballon_140_k.gpx";
		      String gpx_filename = "116_km.gpx";
		      String fit_activity_filename = "Example_Activity.fit";
		      
		      		      
		      try
		      {
		    	  fit_file_encode = new FileEncoder(new java.io.File(fit_activity_filename), Fit.ProtocolVersion.V2_0);
		      }
		      catch (FitRuntimeException e)
		      {
		    	  System.err.println("Error opening file: " + fit_activity_filename);
		    	  return;
		      }

		      //Generate FileIdMessage
		      // Every FIT file MUST contain a 'File ID' message as the first message
		      FileIdMesg fileIdMesg = new FileIdMesg();
		      fileIdMesg.setManufacturer(Manufacturer.DYNASTREAM);
		      fileIdMesg.setType(com.garmin.fit.File.ACTIVITY);
		      fileIdMesg.setProduct(9001);
		      fileIdMesg.setSerialNumber(1701L);

		      fit_file_encode.write(fileIdMesg); // Encode the FileIDMesg

		      RecordMesg fit_record = new RecordMesg();

		      // Dates to be used to generate some sample data;
		      long Start_Time_Millis = System.currentTimeMillis();
		      Timestamp start_Timestamp = new Timestamp(Start_Time_Millis);
		      Calendar start_Calendar = Calendar.getInstance();
		      start_Calendar.setTimeInMillis(start_Timestamp.getTime());
		      System.out.println("start_timestamp = "+ start_Timestamp);
		      



		      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		      try
		      {
		    	  	DocumentBuilder builder = factory.newDocumentBuilder();
		    	  	File fileXML = new File(gpx_filename);
		    	  	Document xml = builder.parse(fileXML);

		            Element root = xml.getDocumentElement();

		            XPathFactory xpf = XPathFactory.newInstance();
		            XPath path = xpf.newXPath();

		            Double current_latitude = (double) 0;
		            Double current_longitude = (double) 0;
		            Double current_elevation = (double) 0;
		            Double current_grade = (double) 0;

		            Double previous_latitude = (double) 0;
		            Double previous_longitude = (double) 0;
		            Double previous_elevation = (double) 0;

		            Double delta_elevation = (double) 0;
		            Double delta_distance_horizontal = (double) 0;
		            Double delta_distance = (double) 0;

		            Double total_distance = (double) 0;
		            
		            String expression = "//trkpt";
		            NodeList list = (NodeList)path.evaluate(expression, root, XPathConstants.NODESET);
		            int nodesLength = list.getLength();

		            for(int i = 0 ; i < nodesLength; i++)
		            {
	            		Element trkpt_Element = (Element) list.item(i);

	            		current_latitude = Double.parseDouble(trkpt_Element.getAttribute("lat"));
	            		current_longitude = Double.parseDouble(trkpt_Element.getAttribute("lon"));
		            	/*current_elevation = (Double)path.evaluate("//trkpt/ele[1]", root, XPathConstants.NUMBER);*/

			            XPathFactory xpf_ele = XPathFactory.newInstance();
			            XPath path_ele = xpf_ele.newXPath();
			            String expression_ele = "./ele";
			            current_elevation = (Double)path.evaluate(expression_ele, trkpt_Element, XPathConstants.NUMBER);

	            		int fit_latitude = 0;
	            		int fit_longitude = 0;
	            		
	            		double current_speed;
			            int delta_time_millisec = 0;
		            	
		            	if (i >0)
		            	{
		            		delta_distance_horizontal = 6378137 * Math.acos(Math.sin(deg2rad(previous_latitude)) * Math.sin(deg2rad(current_latitude)) + Math.cos(deg2rad(previous_latitude)) * Math.cos(deg2rad(current_latitude)) * Math.cos(deg2rad(current_longitude) - deg2rad(previous_longitude)));
		            		delta_elevation = current_elevation - previous_elevation;
		            		delta_distance = Math.sqrt(Math.pow(delta_distance_horizontal, 2) + Math.pow(current_elevation - previous_elevation,2));
		            		total_distance += delta_distance;
		            		current_grade = delta_elevation / delta_distance;

		            		current_speed = speed_m_sec(current_grade);
		            		delta_time_millisec = (int) (delta_distance / current_speed * 1000);
		            		
		            		fit_latitude = (int) (current_latitude * (Math.pow(2, 31) /180));
		            		fit_longitude = (int) (current_longitude * (Math.pow(2, 31) /180));

		            		
		            		start_Calendar.add(Calendar.MILLISECOND, delta_time_millisec);
		            		Timestamp current_Timestamp = new Timestamp(start_Calendar.getTime().getTime());
		            		System.out.println("current_timestamp = "+ current_Timestamp);		            		
		            		
		            		fit_record.setTimestamp(new DateTime(start_Calendar.getTime()));

		            		fit_record.setHeartRate((short)140);
		            		fit_record.setPositionLat(fit_latitude);
		            		fit_record.setPositionLong(fit_longitude);
		            		fit_file_encode.write(fit_record);

		            	}


		            	System.out.println("trkseg # " + i + " : ");

		            	System.out.println("current_latitude = " + current_latitude);
		            	System.out.println("fit_latitude = " + fit_latitude);

		            	System.out.println("current_longitude = " + current_longitude);
		            	System.out.println("fit_longitude = " + fit_longitude);

		            	System.out.println("current_elevation = " + current_elevation);

		            	System.out.println("delta_elevation = " + delta_elevation);
		            	System.out.println("delta_distance = " + delta_distance);
		            	System.out.println("current_grade = " + current_grade);

		            	int test_power = power_W(0 , 0);
		            	
		            	
		            	previous_latitude = current_latitude;
		            	previous_longitude = current_longitude;
		            	previous_elevation = current_elevation;
		            }
		            System.out.println("Total Distance = " + total_distance);
		      }
		      catch (ParserConfigurationException e)
		      {
		          e.printStackTrace();
		      }
		      catch (SAXException e)
		      {
		          e.printStackTrace();
		      }
		      catch (IOException e)
		      {
		          e.printStackTrace();
		      }
		      catch (XPathExpressionException e)
		      {
		          e.printStackTrace();
		       }

		      try
		      {
		    	  fit_file_encode.close();
		      }
		      catch (FitRuntimeException e)
		      {
		    	  System.err.println("Error closing encode.");
		    	  return;
		      }

	   }

	   private static double deg2rad(double deg)
	   {
			return (deg * Math.PI / 180.0);
	   }

	   private static double speed_m_sec(double grade)
	   {
		   double power_W = 0;
		   if (grade < -3)
		   {
			   power_W = 0;
		   }
		   else
		   {
			   if (grade < 0)
			   {
				   power_W = 100;
			   }
			   else
			   {
				   if (grade < 5)
				   {
					   power_W = 200;
				   }
				   else
				   {
					   power_W = 220;
				   }
			   }
		   }

		   double epsilon = 0.000001;
		   
		   double lower_speed_m_s = -50;
		   double upper_speed_m_s = 50;
		   double mid_speed_m_s = 0;
		   
           for(int i = 0 ; i < 100; i++)
           {
    		   mid_speed_m_s = (lower_speed_m_s + upper_speed_m_s) / 2.0;
        	   int mid_power_W = power_W(grade,mid_speed_m_s);
        	   if (Math.abs(mid_power_W - power_W) < epsilon)
        	   {
        		   break;
        	   }
        	   else
        	   {
        		   if (mid_power_W > power_W)
        		   {
        			   upper_speed_m_s = mid_speed_m_s;
        		   }
        		   else
        		   {
        			   lower_speed_m_s = mid_speed_m_s;
        		   }
        	   }
           }
           System.out.println("grade = " + grade + " - power_W = " + power_W + " - mid_speed_km_h = " + mid_speed_m_s * 3600 / 1000.0);
		   return (mid_speed_m_s);
	   }

	   private static int power_W(double grade, double speed_m_s)
	   {
/*
		   speed_m_s = 20.0 * 1000 / 3600;
		   grade = 0.05;
*/
		   int total_power_W = 0;

		   double rider_weight = 75.00;
		   double bike_weight = 8.00;
		   
		   double front_area = 0.60;
		   double drag_coef = 0.63;
		   double drivetrain_loss_percent = 0.03;
		   double rolling_resistance = 0.005;
		   double air_density = 1.226;
		   double gravitational_constant = 9.8067;

		   double total_weight = rider_weight + bike_weight;

		   double power_gravity_W = gravitational_constant * Math.sin(Math.atan(grade)) * total_weight * speed_m_s;
		   double power_rolling_W = gravitational_constant * Math.cos(Math.atan(grade)) * total_weight * rolling_resistance * speed_m_s;
		   double power_drag_W = 0.5 * front_area * drag_coef * air_density * speed_m_s * speed_m_s * speed_m_s;
		   
		   
		   total_power_W = (int) ((power_gravity_W + power_rolling_W + power_drag_W) / (1 - drivetrain_loss_percent));
/*
		   System.out.println("speed_m_s = " + speed_m_s);
		   System.out.println("sin("+ grade + ") = " + Math.sin(grade));

		   System.out.println("power_gravity_W = " + (int) power_gravity_W);
		   System.out.println("power_rolling_W = " + (int) power_rolling_W);
		   System.out.println("power_drag_W = " + (int) power_drag_W);
		   
		   System.out.println("total_power_W = " + total_power_W);
*/
		   return (total_power_W);
	   }	   
	   
}