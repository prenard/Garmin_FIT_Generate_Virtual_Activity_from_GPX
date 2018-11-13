/*

https://openclassrooms.com/fr/courses/2654406-java-et-le-xml

*/

import java.util.*;
import java.util.Date;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

	   public static String start_Timestamp_String = "2018-09-09 10:00:00.0";

	   public static int[] grade_list = {-100,-10,-8,-6,-4,-2,0,2,4,6,8,10,12,14,16,100};

	   //
	   //public static int[] power_list = {1,3,2,100,150,180,200,221,231,220,255,288,300,283};
	   public static int[] power_list = {1,1,1,150,180,200,200,220,220,220,220,220,230,230,230};

	   public static double rider_weight = 77.00;
	   public static double bike_weight = 8.00;
	   public static double equipment_weight = 2.50;
	   
	   public static double front_area = 0.60;
	   public static double drag_coef = 0.63;
	   public static double drivetrain_loss_percent = 0.03;
	   public static double rolling_resistance = 0.005;
	   public static double air_density = 1.226;
	   public static double gravitational_constant = 9.8067;
	   
	   public static PrintWriter FIT_Generate_Virtual_Activity_Log;
	   public static PrintWriter FIT_Generate_Virtual_Activity_CSV;
	   public static PrintWriter FIT_Generate_Virtual_Course_Log;

	   public static int total_speed_problems = 0;
	   
	   public static void main(String[] args)
	   {
		   try
		   {
			   
			   String[] gpx_filename_tokens = args[0].split("\\.(?=[^\\.]+$)");
			   String gpx_filename = args[0];

			   FIT_Generate_Virtual_Activity_Log = new PrintWriter(new BufferedWriter(new FileWriter(gpx_filename_tokens[0] + "_Generate_Virtual_Activity_Log.txt")));
			   FIT_Generate_Virtual_Activity_CSV = new PrintWriter(new BufferedWriter(new FileWriter(gpx_filename_tokens[0] + "_Generate_Virtual_Activity_CSV.csv")));
			   FIT_Generate_Virtual_Course_Log = new PrintWriter(new BufferedWriter(new FileWriter(gpx_filename_tokens[0] + "_Generate_Virtual_Course_Log.txt")));
		   
			   String fit_activity_filename = gpx_filename_tokens[0] + "_Generate_Virtual_Activity.fit";
			   String fit_course_filename = gpx_filename_tokens[0] + "_Generate_Virtual_Course.fit";

			   //String start_Timestamp_String = "2018-09-09 09:00:00.0";

			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FIT Generate Virtual Activity from GPX");
			   WriteLog(FIT_Generate_Virtual_Course_Log, "FIT Generate Virtual Course from GPX");

			   if (grade_list.length != power_list.length + 1)
			   {
				   System.out.println("Error: incorrect grade_list / power_list arrays !!!");
				   return;
			   }
			   
			   FIT_Generate_Virtual_Activity_CSV.println("Timestamp;Distance;Delta_Distance;Elevation;Delta_Elevation;Grade;");
			   
			   System.out.printf("FIT Encode Example Application - Protocol %d.%d Profile %.2f %s\n",Fit.PROTOCOL_VERSION_MAJOR,Fit.PROTOCOL_VERSION_MINOR,Fit.PROFILE_VERSION / 100.0,Fit.PROFILE_TYPE);

			   FileEncoder fit_activity_file_encode;
			   FileEncoder fit_course_file_encode;
		      		      
			   fit_activity_file_encode = new FileEncoder(new java.io.File(fit_activity_filename), Fit.ProtocolVersion.V2_0);
			   fit_course_file_encode = new FileEncoder(new java.io.File(fit_course_filename), Fit.ProtocolVersion.V2_0);

			   Timestamp start_Timestamp = Timestamp.valueOf(start_Timestamp_String);
			   
			   //Generate FileIdMesg - Activity

			   com.garmin.fit.File Activity_Type = com.garmin.fit.File.ACTIVITY;
			   int Manufacturer = com.garmin.fit.Manufacturer.GARMIN;
			   int Product = 2530; // Edge 820
			   long SerialNumber = 1;
			   
			   FileIdMesg fileIdMesg = new FileIdMesg();

			   fileIdMesg.setType(Activity_Type);
			   fileIdMesg.setManufacturer(Manufacturer);
			   fileIdMesg.setProduct(Product);
			   fileIdMesg.setSerialNumber(SerialNumber);
			   fileIdMesg.setTimeCreated(new DateTime(start_Timestamp));

			   fit_activity_file_encode.write(fileIdMesg);

			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FileIdMesg - Activity - Type = " + Activity_Type);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FileIdMesg - Activity - Manufacturer = " + Manufacturer);		      
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FileIdMesg - Activity - Product = " + Product);		      
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FileIdMesg - Activity - SerialNumber = " + SerialNumber);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "FileIdMesg - Activity - TimeCreated = " + start_Timestamp);
			   
			   //Generate FileIdMesg - Course

			   com.garmin.fit.File Course_Type = com.garmin.fit.File.COURSE;

			   fileIdMesg.setType(Course_Type);

			   fit_course_file_encode.write(fileIdMesg);

			   WriteLog(FIT_Generate_Virtual_Course_Log, "FileIdMesg - Course - Type = " + Course_Type);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "FileIdMesg - Course - Manufacturer = " + Manufacturer);		      
			   WriteLog(FIT_Generate_Virtual_Course_Log, "FileIdMesg - Course - Product = " + Product);		      
			   WriteLog(FIT_Generate_Virtual_Course_Log, "FileIdMesg - Course - SerialNumber = " + SerialNumber);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "FileIdMesg - Course - TimeCreated = " + start_Timestamp);

			   
			   //Generate CourseMesg

			   CourseMesg CourseMesg = new CourseMesg();

			   CourseMesg.setName(gpx_filename_tokens[0]);

			   fit_course_file_encode.write(CourseMesg);

			   WriteLog(FIT_Generate_Virtual_Course_Log, "CourseMesg - Name = " + gpx_filename_tokens[0]);

			   //Generate DeviceInfonMesg

			   DeviceInfoMesg DeviceInfoMesg_record = new DeviceInfoMesg();
		      
			   DeviceInfoMesg_record.setTimestamp(new DateTime(start_Timestamp));
			   DeviceInfoMesg_record.setManufacturer(Manufacturer);
			   DeviceInfoMesg_record.setProduct(Product);
			   DeviceInfoMesg_record.setSerialNumber(SerialNumber);
			   fit_activity_file_encode.write(DeviceInfoMesg_record);
		       WriteLog(FIT_Generate_Virtual_Activity_Log, "DeviceInfoMesg - Timestamp = " + start_Timestamp);

			   
			   //Generate EventMesg - Start

			   EventMesg Event_record = new EventMesg();
		      
			   Event_record.setEventType(EventType.START);
			   Event_record.setTimestamp(new DateTime(start_Timestamp));
		      
			   fit_activity_file_encode.write(Event_record);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "EventMesg - Start - Timestamp = " + start_Timestamp);		      

			   fit_course_file_encode.write(Event_record);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "EventMesg - Start - Timestamp = " + start_Timestamp);			   

			   //Generate RecordMesg

			   RecordMesg fit_record = new RecordMesg();			   

			   Calendar start_Calendar = Calendar.getInstance();
			   start_Calendar.setTimeInMillis(start_Timestamp.getTime());
			   //System.out.println("start_timestamp = "+ start_Timestamp);

			   Calendar current_Calendar = Calendar.getInstance();
			   current_Calendar.setTimeInMillis(start_Timestamp.getTime());
		      
			   DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			   DocumentBuilder builder = factory.newDocumentBuilder();
			   File fileXML = new File(gpx_filename);
			   Document xml = builder.parse(fileXML);

		       Element root = xml.getDocumentElement();

		       XPathFactory xpf = XPathFactory.newInstance();
		       XPath path = xpf.newXPath();

		       Double current_latitude = (double) 0;
		       Double current_longitude = (double) 0;
		       int current_fit_latitude = 0;
		       int current_fit_longitude = 0;
		       int current_power_W = 0;
		       
		       Timestamp current_Timestamp = new Timestamp(start_Calendar.getTime().getTime());            		
		       Double current_elevation = (double) 0;

		       Double current_speed;
            		
		       Double previous_latitude = (double) 0;
		       Double previous_longitude = (double) 0;
		       Double previous_elevation = (double) 0;

		       Double current_grade = (double) 0;
		       Double min_current_grade = (double) 0;
		       Double max_current_grade = (double) 0;
		       
		       Double delta_elevation = (double) 0;
		       Double min_delta_elevation = (double) 0;
		       Double max_delta_elevation = (double) 0;

		       Double delta_distance_horizontal = (double) 0;
		       Double delta_distance = (double) 0;

		       Double total_distance_horizontal = (double) 0;
		       Double total_distance = (double) 0;
		       
		       String trkpt_expression = "//trkpt";
		       NodeList trkpt_list = (NodeList)path.evaluate(trkpt_expression, root, XPathConstants.NODESET);
		       int trkpt_list_Length = trkpt_list.getLength();

		       Double total_ascent = (double) 0;
		       Double total_descent = (double) 0;		       

		       List<Stacked_GPX_trkpt> Stacked_GPX_trkpt_list = new ArrayList<Stacked_GPX_trkpt>();
	           System.out.println("Stacked_GPX_trkpt_list.size() = " + Stacked_GPX_trkpt_list.size());

	           List<Stacked_GPX_segment> Stacked_GPX_segment_list = new ArrayList<Stacked_GPX_segment>();
	           
	           // Going to parse the GPX file to generate a trkpt array 

	           WriteLog(FIT_Generate_Virtual_Activity_Log, "==== Parsing GPX file - trkpt_list.getLength() = " + trkpt_list.getLength());	           

	           for(int i = 0 ; i < trkpt_list_Length; i++)
		       {
		    	   System.out.println("trkseg # " + i + " : ");
			            
		           Element trkpt_Element = (Element) trkpt_list.item(i);

		           /* get trkpt attributes */
		            	
		           current_latitude = Double.parseDouble(trkpt_Element.getAttribute("lat"));
		           current_longitude = Double.parseDouble(trkpt_Element.getAttribute("lon"));
		           

		           System.out.println("Stacked_GPX_trkpt_list.size() = " + Stacked_GPX_trkpt_list.size());
		           
		           System.out.println("current_latitude = " + current_latitude);
		           System.out.println("current_longitude = " + current_longitude);

		           /* is-there an elevation element ? */
		            	
		           XPathFactory xpf_ele = XPathFactory.newInstance();
		           XPath path_ele = xpf_ele.newXPath();
		           String ele_expression = "./ele[text()]";
		           //current_elevation = (Double)path.evaluate(expression_ele, trkpt_Element, XPathConstants.NUMBER);
			            
		           Element current_elevation_Element = (Element) path.evaluate(ele_expression, trkpt_Element, XPathConstants.NODE);

		           if (current_elevation_Element == null)
			       {
		        	   /* No, there is no elevation element: use previous_elevation */

		        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Parsing GPX file - trkpt id = " + i + " - No elevation element = Stack this point with null elevation !");		        	   
			           current_elevation = null;
			       }
			       else
			       {
			    	   /* Yes, there is an elevation element */

			           current_elevation = Double.valueOf(current_elevation_Element.getTextContent());
			           //System.out.println("current_elevation = " + current_elevation);

			           WriteLog(FIT_Generate_Virtual_Activity_Log, "Parsing GPX file - trkpt id = " + i + " - elevation = " + current_elevation);
		            	
			       }

		           Stacked_GPX_trkpt new_Stacked_GPX_trkpt = new Stacked_GPX_trkpt(current_latitude,current_longitude,current_elevation);
		           Stacked_GPX_trkpt_list.add(new_Stacked_GPX_trkpt);
		           //WriteLog(FIT_Generate_Virtual_Activity_Log, "Stacked_GPX_trkpt_list.size() = " + Stacked_GPX_trkpt_list.size());
		       }

	           // Going to analyze trkpt array 

	           WriteLog(FIT_Generate_Virtual_Activity_Log, "==== Analysing trkpt array - Stacked_GPX_trkpt_list.size() = " + Stacked_GPX_trkpt_list.size());
	           
	           for(int i = 0 ; i < Stacked_GPX_trkpt_list.size(); i++)
		       {
	        	   Stacked_GPX_trkpt current_Stacked_GPX_trkpt = Stacked_GPX_trkpt_list.get(i);

	        	   current_latitude = current_Stacked_GPX_trkpt.getLatitude();
	        	   current_longitude = current_Stacked_GPX_trkpt.getLongitude();
	        	   current_elevation = current_Stacked_GPX_trkpt.getElevation();

	        	   Double t = (double) 0;
	        	   
	        	   if (i > 0)
	        	   {
		        	   // d= acos( sin(lat1)*sin(lat2)+cos(lat1)*cos(lat2)*cos(lon1-lon2) )
	        		   // More accurate:
	        		   // d= asin( sqrt( ( sin((lat1-lat2)/2))^2 + cos(lat1)*cos(lat2) * (sin((lon1- lon2)/2))^2 ) )
        		   
	        		   //delta_distance_horizontal = 6378137 * Math.acos(Math.sin(deg2rad(previous_latitude)) * Math.sin(deg2rad(current_latitude)) + Math.cos(deg2rad(previous_latitude)) * Math.cos(deg2rad(current_latitude)) * Math.cos(deg2rad(current_longitude) - deg2rad(previous_longitude)));

	        		   t = Math.pow( Math.sin((deg2rad(previous_latitude) - deg2rad(current_latitude)) / 2), (double) 2) + Math.cos(deg2rad(previous_latitude)) * Math.cos(deg2rad(current_latitude)) * Math.pow( Math.sin((deg2rad(previous_longitude) - deg2rad(current_longitude))/2),(double) 2);
	        		   t = 2 * Math.asin( Math.sqrt(t));
	        		   
	        		   delta_distance_horizontal = 6378137 * t;
	        				   
	            	   current_Stacked_GPX_trkpt.setDistance_Horizontal(delta_distance_horizontal);

	            	   if (current_elevation != null)
	            	   {
		            	   delta_elevation = current_elevation - previous_elevation;	            		   
	            	   }
	            	   else
	            	   {
	            		   delta_elevation = null;
	            	   }
	        		   
	        	   }
	        	   else
	        	   {
	        		   delta_distance_horizontal = (double) 0;
	        		   delta_elevation = (double) 0;
	        	   }


	        	   min_delta_elevation = Math.min(min_delta_elevation, delta_elevation);
	        	   max_delta_elevation = Math.max(max_delta_elevation, delta_elevation);
	        	   
	        	   if (delta_elevation > (double) 0)
	        	   {
	        		   total_ascent += delta_elevation;
	        	   }
	        	   else
	        	   {
	        		   total_descent -= delta_elevation; 
	        	   }

	        	   if (delta_distance_horizontal > (double) 0)
	        	   {
	        		   current_grade = delta_elevation / delta_distance_horizontal;		            			
	        	   }
	        	   else
	        	   {
	        		   current_grade = 0.0;
	        	   }

	        	   total_distance_horizontal += delta_distance_horizontal;
	        	   
	        	   min_current_grade = Math.min(min_current_grade, current_grade);
	        	   max_current_grade = Math.max(max_current_grade, current_grade);

	        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Analysing trkpt id = " + i + " - latitude = " + current_latitude + " - longitude = " + current_longitude + "- elevation = " + current_elevation + " - delta_distance_horizontal = " + delta_distance_horizontal + " - delta_elevation = " + delta_elevation + " - current_grade = " + current_grade + " - total_distance_horizontal = " + total_distance_horizontal);	        	   

	        	   previous_latitude = current_latitude;
		           previous_longitude = current_longitude;
		           previous_elevation = current_elevation;
		       }

	           WriteLog(FIT_Generate_Virtual_Activity_Log, "Analysing trkpt - total_distance_horizontal = " + total_distance_horizontal + " - min_delta_elevation = " + min_delta_elevation + " - max_delta_elevation = " + max_delta_elevation + " - min_current_grade = " + min_current_grade + " - max_current_grade = " + max_current_grade);	           

	           // Going to define Segments to avoid peaks 

	           WriteLog(FIT_Generate_Virtual_Activity_Log, "==== Defining Segments ====");
	           
	           int segment_number = 0;

	           Double current_segment_distance_horizontal = (double) 0 ;
	           Boolean next_flag_segment_start = Boolean.TRUE;
	           
	           for(int i = 0 ; i < Stacked_GPX_trkpt_list.size(); i++)
		       {
	        	   Stacked_GPX_trkpt current_Stacked_GPX_trkpt = Stacked_GPX_trkpt_list.get(i);

	        	   if (next_flag_segment_start)
	        	   {
			           Stacked_GPX_segment new_Stacked_GPX_segment = new Stacked_GPX_segment(i);
			           Stacked_GPX_segment_list.add(new_Stacked_GPX_segment);
	        		   current_segment_distance_horizontal = (double) 0;
			           if (i == 0)
		        	   {
		        		   Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setStart_Elevation(current_Stacked_GPX_trkpt.getElevation());
		        	   }
		        	   else
		        	   {
		        		   Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setStart_Elevation(Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 2).getEnd_Elevation());
		        	   }
			           WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating segment id = " + (Stacked_GPX_segment_list.size() - 1) + " - start trkpt id = " + i + " - start elevation = " + Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).getStart_Elevation());
	        	   }
	        	   
	        	   
	        	   if (i == 0)
	        	   {
	        		   next_flag_segment_start = Boolean.FALSE;
	        		   current_segment_distance_horizontal = (double) 0;
	        	   }
	        	   else
	        	   {
	        		   current_segment_distance_horizontal += current_Stacked_GPX_trkpt.getDistance_Horizontal();

	        		   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating segment id = " + (Stacked_GPX_segment_list.size() - 1) + " - trkpt id = " + i + " - current_segment_distance_horizontal = " + current_segment_distance_horizontal + " - current elevation = " + current_Stacked_GPX_trkpt.getElevation());    	        	   

	        		   if (current_segment_distance_horizontal > (double) 100)
	        		   {
	    	        	   Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setEnd_trkpt_id(i);
	    	        	   Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setDistance_Horizontal(current_segment_distance_horizontal);;
				           Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setEnd_Elevation(current_Stacked_GPX_trkpt.getElevation());
				           
	    	        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating segment id = " + (Stacked_GPX_segment_list.size() - 1) + " - stop trkpt id = " + i + " - end elevation = " + Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).getEnd_Elevation());
	    	        	   segment_number++;
	    	        	   next_flag_segment_start = Boolean.TRUE;
	        		   }
	        		   else
	        		   {
		        		   next_flag_segment_start = Boolean.FALSE;
	        		   }
	        	   }

	        	   /* last trkpt = close last segment */
	        	   if (i == (Stacked_GPX_trkpt_list.size() - 1))
	        	   {
	    	           Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setEnd_trkpt_id(i);
	    	           Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setDistance_Horizontal(current_segment_distance_horizontal);
	    	           Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).setEnd_Elevation(Stacked_GPX_trkpt_list.get(i).getElevation());
    	        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating segment id = " + (Stacked_GPX_segment_list.size() - 1) + " - stop trkpt id = " + i + " - end elevation = " + Stacked_GPX_segment_list.get(Stacked_GPX_segment_list.size() - 1).getEnd_Elevation());
	        	   }
		       }

        	   // Going to analyse Segments to avoid peaks 

	           WriteLog(FIT_Generate_Virtual_Activity_Log, "==== Analysing Segments ====");
	           
	           for(int i = 0 ; i < Stacked_GPX_segment_list.size(); i++)
		       {
	        	   Stacked_GPX_segment current_Stacked_GPX_segment = Stacked_GPX_segment_list.get(i);

	        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Analysing segment id = " + i + " - start trkpt id = " + current_Stacked_GPX_segment.getStart_trkpt_id() + " - end trkpt id = " + current_Stacked_GPX_segment.getEnd_trkpt_id() + " - distance_horizontal = " + current_Stacked_GPX_segment.getDistance_Horizontal() + " - grade = " + current_Stacked_GPX_segment.getGrade());

		       }

	           // Going to generate fit records 
	           
	           WriteLog(FIT_Generate_Virtual_Activity_Log, "==== Generating FIT records ====");
	           
	           total_distance = (double) 0;
	           total_ascent = (double) 0;
	           total_descent = (double) 0;
	           total_speed_problems = 0;
	           
	           Double last_trkpt_remaining_delta_distance = (double) 0;
	           
	           for(int i = 0 ; i < Stacked_GPX_segment_list.size(); i++)
		       {
	        	   Stacked_GPX_segment current_Stacked_GPX_segment = Stacked_GPX_segment_list.get(i);

	        	   current_grade = current_Stacked_GPX_segment.getGrade(); 
	        	   current_speed = speed_m_sec(current_grade);
	        	   
	        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating FIT records - segment id = " + i + " - start trkpt id = " + current_Stacked_GPX_segment.getStart_trkpt_id() + " - end trkpt id = " + current_Stacked_GPX_segment.getEnd_trkpt_id() + " - distance_horizontal = " + current_Stacked_GPX_segment.getDistance_Horizontal() + " - delta_elevation = " + current_Stacked_GPX_segment.getDelta_Elevation() + " - grade = " + current_Stacked_GPX_segment.getGrade() + " - speed = " + current_speed);

	        	   for(int j = current_Stacked_GPX_segment.getStart_trkpt_id() ; j <= current_Stacked_GPX_segment.getEnd_trkpt_id(); j++)
			       {
	        		   if (j > 0)
	        		   {
		        		   Stacked_GPX_trkpt current_Stacked_GPX_trkpt = Stacked_GPX_trkpt_list.get(j);
		        		   Stacked_GPX_trkpt previous_Stacked_GPX_trkpt = Stacked_GPX_trkpt_list.get(j-1);

				           current_fit_latitude = (int) (current_Stacked_GPX_trkpt.getLatitude() * (Math.pow(2, 31) /180));
				           current_fit_longitude = (int) (current_Stacked_GPX_trkpt.getLongitude() * (Math.pow(2, 31) /180));
				           current_elevation = current_Stacked_GPX_trkpt.getElevation();

				           delta_distance_horizontal = current_Stacked_GPX_trkpt.getDistance_Horizontal();
				           //delta_elevation = current_Stacked_GPX_trkpt.getElevation() - previous_Stacked_GPX_trkpt.getElevation();
				           delta_elevation = current_Stacked_GPX_segment.getDelta_Elevation() * current_Stacked_GPX_trkpt.getDistance_Horizontal() / current_Stacked_GPX_segment.getDistance_Horizontal();
				           delta_distance = Math.sqrt(Math.pow(delta_distance_horizontal, 2) + Math.pow(delta_elevation,2));
			        	   
			        	   delta_distance += last_trkpt_remaining_delta_distance;
			        	   
			        	   int delta_time_millisec = (int) (delta_distance / current_speed * 1000);
			        	   
			        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating FIT records - trkpt id = " + j + " - delta_distance = " + delta_distance + " - delta_elevation = " + delta_elevation + " - speed = " + current_speed + " - delta_time sec = " + (delta_time_millisec / 1000));

			        	   // to avoid duplicate records in the same second
			        	   
			        	   if (delta_time_millisec > 1000 * 1)
			        	   {
			            	   current_Calendar.add(Calendar.MILLISECOND, delta_time_millisec);
			            	   current_Timestamp = new Timestamp(current_Calendar.getTime().getTime());
			            	   //System.out.println("current_timestamp = "+ current_Timestamp);		            		

			            	   total_distance = total_distance + delta_distance;

			            	   if (delta_elevation > 0)
			            	   {
			            		   total_ascent += delta_elevation;
			            	   }
			            	   else
			            	   {
			            		   total_descent -= delta_elevation; 
			            	   }

			            	   current_power_W = power_W(current_grade);
			            	   
			            	   fit_record.setTimestamp(new DateTime(current_Timestamp));

			            	   fit_record.setPositionLat(current_fit_latitude);
			            	   fit_record.setPositionLong(current_fit_longitude);
			            	   fit_record.setSpeed(current_speed.floatValue());
			            	   fit_record.setDistance(total_distance.floatValue());
			            	   fit_record.setPower(power_W(current_grade));

			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Timestamp = " + current_Timestamp);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - ElapdedTime = " + ElapsedTime(start_Calendar, current_Calendar));
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "trkpt_id = " + j);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "gpx - Grade = " + current_grade);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Latitude = " + current_fit_latitude);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Longitude = " + current_fit_longitude);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Speed = " + current_speed);
			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Power = " + current_power_W);

			            	   // to minimize wrong elevation add only altitude info to segment start / end
			            	   
			            	   if ((j == current_Stacked_GPX_segment.getStart_trkpt_id()) || (j == current_Stacked_GPX_segment.getEnd_trkpt_id()))
			            	   {
				            	   fit_record.setAltitude(current_elevation.floatValue());			            		   
				            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Altitude = " + current_elevation + " - delta_elevation = " + delta_elevation);
			            	   }
			            	   
			            	   fit_activity_file_encode.write(fit_record);
			            	   fit_course_file_encode.write(fit_record);
			            	   

			            	   WriteLog(FIT_Generate_Virtual_Activity_Log, "RecordMesg - Distance = " + total_distance + " - delta_distance = " + delta_distance);

			            	   FIT_Generate_Virtual_Activity_CSV.println(current_Timestamp + ";" + total_distance + ";" + delta_distance + ";" + current_elevation + ";" + delta_elevation + ";" + current_grade + ";");
			    			   
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "RecordMesg - Timestamp = " + current_Timestamp);
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "trkpt_id = " + j);
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "RecordMesg - Speed = " + current_speed);
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "RecordMesg - Distance = " + total_distance);
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "RecordMesg - Latitude = " + current_fit_latitude);
			            	   WriteLog(FIT_Generate_Virtual_Course_Log, "RecordMesg - Longitude = " + current_fit_longitude);
			            	   
			            	   last_trkpt_remaining_delta_distance = (double) 0;
			        	   }
			        	   else
			        	   {
			        		   last_trkpt_remaining_delta_distance = delta_distance;
				        	   WriteLog(FIT_Generate_Virtual_Activity_Log, "Generating FIT records - trkpt id = " + j + " - last_trkpt_remaining_delta_distance = " + last_trkpt_remaining_delta_distance);
			        	   }
	
	        		   }
			       }
	        	   
		       }
	           
	           //Generate EventMesg - Stop_All
	      
			   Event_record.setEventType(EventType.STOP_ALL);
			   Event_record.setTimestamp(new DateTime(current_Timestamp));
		      
			   fit_activity_file_encode.write(Event_record);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "EventMesg - Stop_All - Timestamp = " + current_Timestamp);		      

			   fit_course_file_encode.write(Event_record);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "EventMesg - Stop_All - Timestamp = " + current_Timestamp);			   
		       
		       
		       //Generate LapMesg - Stop

		       Double total_duration_s = (current_Calendar.getTime().getTime() - start_Calendar.getTime().getTime()) / 1000.0;

		       Float avg_speed = total_distance.floatValue() / total_duration_s.floatValue();
		       
		       LapMesg LapMesg_record = new LapMesg();

		       LapMesg_record.setEvent(Event.LAP);
			   LapMesg_record.setEventType(EventType.STOP);
			   LapMesg_record.setTimestamp(new DateTime(current_Timestamp));
			   LapMesg_record.setStartTime(new DateTime(start_Timestamp));
		       LapMesg_record.setTotalTimerTime(total_duration_s.floatValue());			   
		       LapMesg_record.setTotalElapsedTime(total_duration_s.floatValue());
		       LapMesg_record.setTotalDistance(total_distance.floatValue());
		       LapMesg_record.setAvgSpeed(avg_speed);
		       LapMesg_record.setTotalAscent(total_ascent.intValue());
		       LapMesg_record.setTotalDescent(total_descent.intValue());
		       
		       fit_activity_file_encode.write(LapMesg_record);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - Timestamp = " + current_Timestamp);			   
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - Starttime = " + start_Timestamp);			   
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - TotalTimerTime = " + total_duration_s);			   
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - TotalDistance = " + total_distance);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - AvgSpeed - m/s = " + avg_speed);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - TotalAscent = " + total_ascent);
			   WriteLog(FIT_Generate_Virtual_Activity_Log, "LapMesg - STOP - TotalDescent = " + total_descent);
			   
		       fit_course_file_encode.write(LapMesg_record);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - Timestamp = " + current_Timestamp);			   
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - Starttime = " + start_Timestamp);			   
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - TotalTimerTime = " + total_duration_s);			   
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - TotalDistance = " + total_distance);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - AvgSpeed - m/s = " + avg_speed);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - TotalAscent = " + total_ascent);
			   WriteLog(FIT_Generate_Virtual_Course_Log, "LapMesg - STOP - TotalDescent = " + total_descent);
			   
		       //Generate SessionMesg - Stop

		       Sport Activity_Sport = com.garmin.fit.Sport.CYCLING;
		       
			   SessionMesg Session_record = new SessionMesg();

			   Session_record.setEvent(Event.SESSION);
			   Session_record.setEventType(EventType.STOP);
			   Session_record.setTimestamp(new DateTime(current_Timestamp));
			   Session_record.setStartTime(new DateTime(start_Timestamp));
			   Session_record.setSport(Activity_Sport);
			   Session_record.setTotalElapsedTime(total_duration_s.floatValue());
			   
			   fit_activity_file_encode.write(Session_record);
		       WriteLog(FIT_Generate_Virtual_Activity_Log, "SessionMesg - Timestamp = " + current_Timestamp);
		       WriteLog(FIT_Generate_Virtual_Activity_Log, "SessionMesg - StartTime = " + start_Timestamp);
		       WriteLog(FIT_Generate_Virtual_Activity_Log, "SessionMesg - Sport = " + Activity_Sport);

		       
		       System.out.println("Total Distance = " + total_distance);

		       System.out.println("start - sec = " + start_Calendar.getTime().getTime());
		       System.out.println("current - sec = " + current_Calendar.getTime().getTime());
		       System.out.println("Total Duration - sec = " + total_duration_s);

		       
		       // Offset between Garmin 
		       // (FIT) time and Unix 
		       // time in ms (Dec 31, 
		       // 1989 - 00:00:00 
		       // January 1, 1970). 			  
		       // https://www.programcreek.com/java-api-examples/index.php?source_dir=wattzap-ce-master/src/com/wattzap/utils/FitImporter.java
					  
					  
		       long Garmin_epoch_offset_millisec = 631065600000l;
		       long Garmin_epoch_LocalTimestamp = (current_Calendar.getTime().getTime() - Garmin_epoch_offset_millisec) / 1000;
		       System.out.println("Garmin_epoch_LocalTimestamp - millisec = " + Garmin_epoch_LocalTimestamp);

		       //Generate ActivityMesg - Stop
		       
		       ActivityMesg activity_record = new ActivityMesg();

		       activity_record.setEvent(Event.ACTIVITY);
		       activity_record.setType(Activity.MANUAL);
		       activity_record.setTimestamp(new DateTime(current_Timestamp));
		       activity_record.setLocalTimestamp(Garmin_epoch_LocalTimestamp);
		       activity_record.setEventType(EventType.STOP);
		       activity_record.setTotalTimerTime(total_duration_s.floatValue());
		       activity_record.setNumSessions(1);
		       fit_activity_file_encode.write(activity_record);

		       WriteLog(FIT_Generate_Virtual_Activity_Log, "ActivityMesg - Timestamp = " + current_Timestamp);

		       System.out.println("ElapsedTime = " + ElapsedTime(start_Calendar, current_Calendar));
		       System.out.println("total_ascent = " + total_ascent);
		       System.out.println("total_descent = " + total_descent);
		       System.out.println("total_speed_problems = " + total_speed_problems);


		       fit_activity_file_encode.close();
		       fit_course_file_encode.close();

		       FIT_Generate_Virtual_Activity_Log.close();
		       FIT_Generate_Virtual_Activity_CSV.close();
		       FIT_Generate_Virtual_Course_Log.close();

		   }
		   catch(Exception err)
		   {
			   err.printStackTrace();
		   }
	   }

	   private static String ElapsedTime(Calendar c1, Calendar c2)
	   {
		   String difference = "";

		   long secondsInMilli = 1000;
		   long minutesInMilli = secondsInMilli * 60;
		   long hoursInMilli = minutesInMilli * 60;
		   long daysInMilli = hoursInMilli * 24;

		   long different = c2.getTimeInMillis() - c1.getTimeInMillis();
			
		   long elapsedDays = different / daysInMilli;
		   different = different % daysInMilli;
			
		   long elapsedHours = different / hoursInMilli;
		   different = different % hoursInMilli;
			
		   long elapsedMinutes = different / minutesInMilli;
		   different = different % minutesInMilli;
			
		   long elapsedSeconds = different / secondsInMilli;
			
		   difference = String.format("%d days, %d hours, %d minutes, %d seconds", elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
/*
		   System.out.printf(
			    "%d days, %d hours, %d minutes, %d seconds%n", 
			    elapsedDays,
			    elapsedHours, elapsedMinutes, elapsedSeconds);		   
*/
		   return difference;
	   }

	   
	   private static double deg2rad(double deg)
	   {
		   return (deg * Math.PI / 180.0);
	   }

	   private static int power_W(double grade)
	   {
		   //int[] grade_list = {-100,-10,-8,-6,-4,-2,0,2,4,6,8,10,12,14,16,100};
		   //int[] power_list = {0,3,2,100,150,180,200,221,231,220,255,288,300,283};
		   
		   int power_W = 0;

		   //System.out.println("grade = " + grade);
	       
	       for(int i = 0 ; i < grade_list.length - 1; i++)
	       {
	    	   if (grade >= (grade_list[i] / 100.0) && (grade < grade_list[i+1] / 100.0))
	    	   {
	    		   power_W = power_list[i];
	    		   break;
	    	   }
	       }

	       //System.out.println("power_W = " + power_W);
	       
	       return (power_W);
	   }

	   private static double speed_m_sec(double grade)
	   {
		   int power_W = power_W(grade);
		   
		   WriteLog(FIT_Generate_Virtual_Activity_Log, "speed_m_sec - grade = " + grade);
		   WriteLog(FIT_Generate_Virtual_Activity_Log, "speed_m_sec - power_W = " + power_W);
		   
		   double epsilon = 0.000001;
		   
		   double lower_speed_m_s = -50;
		   double upper_speed_m_s = 50;
		   double mid_speed_m_s = 0;
		   
           for(int i = 0 ; i < 100; i++)
           {
    		   mid_speed_m_s = (lower_speed_m_s + upper_speed_m_s) / 2.0;
        	   int mid_power_W = power_W(grade,mid_speed_m_s);
    		   //WriteLog(FIT_Generate_Virtual_Activity_Log, "speed_m_sec - mid_speed_m_s = " + mid_speed_m_s + " - mid_power_W = " + mid_power_W);
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
		   //WriteLog(FIT_Generate_Virtual_Activity_Log,"speed_m_sec - grade = " + grade + " - power_W = " + power_W + " - mid_speed_km_h = " + mid_speed_m_s * 3600 / 1000.0);
           if (mid_speed_m_s == 0)
           {
        	   mid_speed_m_s = 10 * 1000 / 3600;
    		   WriteLog(FIT_Generate_Virtual_Activity_Log, "### Speed Problem : grade = " + grade);
        	   total_speed_problems++;
           }
           return (mid_speed_m_s);
	   }

	   private static int power_W(double grade, double speed_m_s)
	   {
/*
		   speed_m_s = 20.0 * 1000 / 3600;
		   grade = 0.05;
*/
		   //WriteLog(FIT_Generate_Virtual_Activity_Log, "power_W - grade = " + grade + " - speed_m_s = " + speed_m_s);
		   
		   int total_power_W = 0;
/*
		   double rider_weight = 75.00;
		   double bike_weight = 8.00;
		   
		   double front_area = 0.60;
		   double drag_coef = 0.63;
		   double drivetrain_loss_percent = 0.03;
		   double rolling_resistance = 0.005;
		   double air_density = 1.226;
		   double gravitational_constant = 9.8067;
*/
		   double total_weight = rider_weight + bike_weight + equipment_weight;

		   double power_gravity_W = gravitational_constant * Math.sin(Math.atan(grade)) * total_weight * speed_m_s;
		   double power_rolling_W = gravitational_constant * Math.cos(Math.atan(grade)) * total_weight * rolling_resistance * speed_m_s;
		   double power_drag_W = 0.5 * front_area * drag_coef * air_density * speed_m_s * speed_m_s * speed_m_s;
		   
		   
		   total_power_W = (int) ((power_gravity_W + power_rolling_W + power_drag_W) / (1 - drivetrain_loss_percent));

		   //WriteLog(FIT_Generate_Virtual_Activity_Log, "power_W - power_gravity_W = " + power_gravity_W + " - power_rolling_W = " + power_rolling_W + " - power_drag_W = " + power_drag_W);
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

	   public static void WriteLog(PrintWriter Log, String Line)
	   {
		   try
		   {
			   // Get date and time
			   Date today = new Date();
			   SimpleDateFormat MyFormatter = new SimpleDateFormat ("yyyy'-'MM'-'dd HH:mm:ss");
			   String DatePrefix = MyFormatter.format(today);
			   Log.println(DatePrefix + " - " + Line);
		   }
		   catch (Exception err)
		   {
			   err.printStackTrace();
		   }
	   }
}