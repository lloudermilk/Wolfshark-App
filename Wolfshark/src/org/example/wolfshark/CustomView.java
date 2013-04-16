package org.example.wolfshark;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class CustomView extends View {
	
	BitmapFactory.Options options = new BitmapFactory.Options();
	
	Paint imgPaint;
	Bitmap mapGraphic_1;
	Bitmap mapGraphic_2;
	Bitmap mapGraphic_3;
	Bitmap mapGraphic_4;
	Path thePath;
	Paint pathPaint;
	Path startCircle;
	Path endCircle;
	
	//int numNodesToRemove;  //ONLY USE INSIDE DEPTHFIRSTSEARCHCOMPLETE!!!
	
	private int[] xcoord_primarray;
	private int[] ycoord_primarray;
	
	private Integer[] xcoord_array;
	private Integer[] ycoord_array;
	
	public static final String PREFERENCE_FILENAME = "LocatePrefs";
	SharedPreferences myPrefs;

	int mapX = 0;
	int mapY = 0;
	
	int testx = 0;
	int testy = 0;
	
	public int offsetX = 0;
	public int offsetY = 0;

	int startX;
	int startY;
	
	final int locate_x = 840;
	final int locate_y = 860;
	
	int coord_x = 0;
	int coord_y = 0;
	
	DisplayMetrics metrics;
	
	boolean moved = false;
	
	int map1width;
	int map1height;
	
	Paint dotPaint;
	Paint textPaint;
	Paint circlePaint;
	
	NodeGraph network;
	CampusNodeConfig config;
	NodeConfig oldConfig;
	
	//String startLocation;
	//String endLocation;
	
	boolean endLocationFound = false;
	
	ArrayList<Integer> vistiedList = new ArrayList<Integer>();
	ArrayList<Integer> explored = new ArrayList<Integer>();
	ArrayList<Node> exploredNodes = new ArrayList<Node>();
	
	ArrayList<String> startentrances = new ArrayList<String>();
	ArrayList<String> endentrances = new ArrayList<String>();
	
	String start_build;
	String end_build;
	String start_room;
	String end_room;
	int start_pos;
	int end_pos;
	
	public CustomView(Context myContext)
	{
		super(myContext);
		this.setBackgroundColor(Color.WHITE);
		
		myPrefs = myContext.getSharedPreferences(PREFERENCE_FILENAME,0);
		//startLocation=myPrefs.getString("startBuild", "Darwin_022");
		//endLocation=myPrefs.getString("endBuild", "Darwin_006");		
		
		start_build = "";
		end_build = "";
		start_room = "";
		end_room = "";
		
		start_build = myPrefs.getString("startBuild", "None");
		end_build = myPrefs.getString("endBuild", "None");
		start_room = myPrefs.getString("startRoom", "None");
		end_room = myPrefs.getString("endRoom", "None");
		start_pos = myPrefs.getInt("startPos", 0);
		end_pos = myPrefs.getInt("endPos", 0);
		
		startentrances = getEntranceList(start_pos);
		endentrances = getEntranceList(end_pos);
		//numNodesToRemove = 0;
		
		network = new NodeGraph();
		config = new CampusNodeConfig();
		oldConfig = new NodeConfig();
		//network = config.populate(myContext);
		network = config.populate();
		
		Log.d("GOT","HERE");
		
		
		//options.inSampleSize = 2;
		// sets bitmap density to display density, stopping lag and reducing memory footprint
		options.inDensity =  getResources().getDisplayMetrics().densityDpi; 
		
		dotPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
		dotPaint.setColor(Color.BLACK);
		dotPaint.setStyle(Paint.Style.FILL);
		
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor(Color.BLUE);
		circlePaint.setStyle(Paint.Style.FILL);
		
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.RED);
		textPaint.setTextSize(10);
		
		Log.d("GOT","HERE2");
		
		mapGraphic_1 = BitmapFactory.decodeResource(getResources(), R.drawable.campus_map_nw, options);
		mapGraphic_2 = BitmapFactory.decodeResource(getResources(), R.drawable.campus_map_ne, options);
		mapGraphic_3 = BitmapFactory.decodeResource(getResources(), R.drawable.campus_map_sw, options);
		mapGraphic_4 = BitmapFactory.decodeResource(getResources(), R.drawable.campus_map_se, options);
		
		map1width = mapGraphic_1.getWidth();
		map1height = mapGraphic_1.getHeight();
		
		metrics = getResources().getDisplayMetrics();
		
		imgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		thePath = new Path();
		startCircle = new Path();
		endCircle = new Path();
		
		xcoord_primarray = getResources().getIntArray(R.array.xcoord_array);
		ycoord_primarray = getResources().getIntArray(R.array.ycoord_array);
		

		
		
		Log.d("GOT","HERE3");
//		pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		pathPaint.setColor(Color.BLACK);
//		pathPaint.setStrokeWidth(3);
//		pathPaint.setStyle(Paint.Style.STROKE);
		
		
		pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pathPaint.setColor(Color.BLUE);
		pathPaint.setStrokeWidth(3);
		pathPaint.setStyle(Paint.Style.STROKE);
		
		coord_x = (mapGraphic_1.getWidth() - 1200) * (400 / mapGraphic_1.getDensity());
		coord_y = (mapGraphic_1.getHeight() - 1200) * (400 / mapGraphic_1.getDensity());
		
		//startLocation = "darwin_" + startLocation;
		//endLocation = "darwin_" + endLocation;
		
		//Log.d("DEBUG","StartLocation: " + startLocation);
		//Log.d("DEBUG","EndLocation: " + endLocation);
		
		for(int i=0;i < network.NodeList.size();i++)
		{
			//Log.d("DEBUG","Start Search: " + network.NodeList.get(i).Name);
			
			//if(network.NodeList.get(i).Name.contains(startLocation))
			if(network.NodeList.get(i).contains(startentrances))
			{
				//depthFirstSearch(network.NodeList.get(i));
				depthFirstSearchComplete(network.NodeList.get(i));
				break;
			}
		}
		

		
		// given the algorithm, a stubNode is a node that only has one neighbor
		// this stubNodesExistDeleteThem method returns true upon finding a stub node, 
		// false upon looping through all the nodes and not finding a stub node.
		// this while loop will remove all stub nodes from the exploredNodes list
		while(stubNodesExistDeleteThem() == true) {} //looping over method call, returns true if method is successful

		
		//still need to check this out...
		//I believe we can adjust the code in onDraw() such that we don't need this loop or vistied. ~ Jolie
		for(int i=0;i<exploredNodes.size();i++)
		{
			for(int j=0;j<network.NodeList.size();j++)
			{
				if(network.NodeList.get(j).Name == exploredNodes.get(i).Name)
				{
					vistiedList.add(j);
				}
			}
		}
		
		
		Log.d("EXPLORED NODES: ", " ");
		
		for(int i = 0; i < exploredNodes.size(); i++)
		{
			Log.d("NODE NAME/#:  ", exploredNodes.get(i).Name + "  " + i);
		}
		
		Log.d("DEBUG","Size of exploredList:" + exploredNodes.size());
		
		Log.d("DEBUG","Size of visitedList:" + vistiedList.size());
		
		Log.d("START NODE: ", startentrances.get(0));
		Log.d("END NODE: ", endentrances.get(0));
		Log.d("START POS: ", start_pos + " ");
		Log.d("END POS: ", end_pos + " ");
		Log.d("SIZE OF VISITED:  ", vistiedList.size() + " ");
	}
	
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		//long startTime = System.nanoTime();
		
		

		
		canvas.drawBitmap(mapGraphic_1, mapX + offsetX , mapY + offsetY, imgPaint);
		canvas.drawBitmap(mapGraphic_2, mapX + offsetX + map1width + 1, mapY + offsetY, imgPaint);
		canvas.drawBitmap(mapGraphic_3, mapX + offsetX, mapY + offsetY + mapGraphic_1.getHeight(), imgPaint);
		canvas.drawBitmap(mapGraphic_4, mapX + offsetX + mapGraphic_1.getWidth(), mapY + offsetY + mapGraphic_2.getHeight(), imgPaint);
		
		thePath.reset();
		startCircle.reset();
		endCircle.reset();
		
		startCircle.addCircle(xcoord_primarray[start_pos] + mapX + offsetX, ycoord_primarray[start_pos] + mapY + offsetY, 20, Direction.CW);
		endCircle.addCircle(xcoord_primarray[end_pos] + mapX + offsetX, ycoord_primarray[end_pos] + mapY + offsetY, 20, Direction.CW);
		
		if(vistiedList.size() >= 2)
		{
			if(vistiedList.size() > 0)
			{
				thePath.moveTo(network.NodeList.get(vistiedList.get(1)).mapAnchorX+offsetX+mapX, 
					       network.NodeList.get(vistiedList.get(1)).mapAnchorY+offsetY+mapY);
			}
			for(int i=0;i<vistiedList.size();i++)
			{
				thePath.lineTo(network.NodeList.get(vistiedList.get(i)).mapAnchorX+offsetX+mapX, 
					       network.NodeList.get(vistiedList.get(i)).mapAnchorY+offsetY+mapY);
			}
			
		}
		for(int i=0;i<network.NodeList.size();i++)
		{
//			canvas.drawCircle(network.NodeList.get(i).mapAnchorX+offsetX+mapX, network.NodeList.get(i).mapAnchorY+offsetY+mapY, 5, dotPaint);
//			canvas.drawText(network.NodeList.get(i).Name + " " + i, network.NodeList.get(i).mapAnchorX+offsetX+mapX+7, 
//					network.NodeList.get(i).mapAnchorY+offsetY+mapY+7, textPaint);
		}
		canvas.drawPath(thePath, pathPaint);
		canvas.drawPath(startCircle, circlePaint);
		canvas.drawPath(endCircle, circlePaint);
		
		
//		thePath.moveTo(250+offsetX+mapX, 250+offsetY+mapY);
//		thePath.lineTo(200+offsetX+mapX, 200+offsetY+mapY);
//		thePath.lineTo(600+offsetX+mapX, 200+offsetY+mapY);
//		thePath.lineTo(300+offsetX+mapX, 1200+offsetY+mapY);
		
		//thePath.addCircle(locate_x + mapX + offsetX + coord_x, locate_y + mapY + offsetY + coord_y, 25, Path.Direction.CW);
		//canvas.drawPath(thePath, pathPaint);
		
		//long endTime = System.nanoTime();
		
		//Log.d("TIMING","draw execution time: "+ (endTime - startTime));
	
	}
	
	
	
	
	
	public void moveMap(int inputX, int inputY)
	{


	}
	
	
	
	
	
	public int getMapX()
	{
		return mapX;
	}
	
	
	
	
	
	public int getMapY()
	{
		return mapY;
	}
	
	
	
	
	
	private boolean containsByName(String Input)
	{
		for(int j=0; j<exploredNodes.size();j++)
		{
			if(exploredNodes.get(j).Name.contains(Input) == true)
			{
				return true;
			}
		}
		return false;
	}
	
	
	
	
	private boolean stubNodesExistDeleteThem()
	{
		int connectedNodes = 0;
		//boolean removedNodes = false;
		
		for(int i=0;i<exploredNodes.size();i++)
		{
			connectedNodes = 0;
			
			for(int j=0; j<exploredNodes.get(i).neighboringNodes.size();j++)
			{
				if(containsByName(exploredNodes.get(i).neighboringNodes.get(j)) == true )
				{
					connectedNodes++;
				}
			}
			//if(connectedNodes == 1 && exploredNodes.get(i).Name.contains(startLocation) == false &&	exploredNodes.get(i).Name.contains(endLocation) == false)
			if(connectedNodes == 1 && exploredNodes.get(i).contains(startentrances) == false &&	exploredNodes.get(i).contains(endentrances) == false)
			{
				exploredNodes.remove(i);
				return true;
			}
			
		}
		

		return false;
		
		
	}
	
	
	
	
	private void depthFirstSearch(Node inputNode)
	{
		exploredNodes.add(inputNode);
		//if(inputNode.Name.contains(endLocation))
		if(inputNode.contains(endentrances))
		{
			return;
		}
		for(int i=0;i<inputNode.neighboringNodes.size();i++)
		{
			for(int j=0;j<network.NodeList.size();j++)
			{
				if(inputNode.neighboringNodes.get(i) == network.NodeList.get(j).Name)
				{				
					if (exploredNodes.contains(network.NodeList.get(j)))
                    {
                    				continue;
                   	}
					depthFirstSearch(network.NodeList.get(j));
				}
			}
		}
		
	}
	
	
	
	/*
	private void depthFirstSearchComplete(Node inputNode)
	{
		//Log.d("DEBUG", "search called on node:" + inputNode.Name);

		//if(inputNode.Name.contains(endLocation))
		if(inputNode.contains(endentrances))  //If the inputNode is in the list of end location entrances
		{
			endLocationFound = true;
		}
		if(exploredNodes.contains(inputNode) == false)  //If the inputNode is not in the list of exploredNodes
		{
			exploredNodes.add(inputNode);
		}
		//If exploredNodes does contain the inputNode
		else if(exploredNodes.contains(inputNode) == true)  //Can these two expressions be consolidated using an ||??
		{
			return;
		}
		if(endLocationFound == true)
		{
			return;
		}
		for(int i=0;i<inputNode.neighboringNodes.size();i++)
		{
			for(int j=0;j<network.NodeList.size();j++)
			{
				//if(inputNode.neighboringNodes.get(i) == network.NodeList.get(j).Name) 
				//If inputNode matches the name of a node in the network and if the end entrance is not in the exploredNodes Arraylist
				if(inputNode.neighboringNodes.get(i).equals(network.NodeList.get(j).Name) && !exploredNodes.get(exploredNodes.size()-1).contains(endentrances))
				{
					depthFirstSearchComplete(network.NodeList.get(j));
				}

			}
		}
	}
	*/
	
	
	//TEST VERSION!!!
	private void depthFirstSearchComplete(Node inputNode)
	{
		//Log.d("DEBUG", "search called on node:" + inputNode.Name);

		//if(inputNode.Name.contains(endLocation))
		if(inputNode.contains(endentrances))  //If the inputNode is in the list of end location entrances
		{
			endLocationFound = true;
		}
		if(exploredNodes.contains(inputNode) == false)  //If the inputNode is not in the list of exploredNodes
		{
			exploredNodes.add(inputNode);
		}
		//If exploredNodes does contain the inputNode
		else if(exploredNodes.contains(inputNode) == true)  //Can these two expressions be consolidated using an ||??
		{
			return;
		}
		if(endLocationFound == true)
		{
			return;
		}
		
		//If an ending destination is in the list of neighboring nodes
		//If checkForNeighboringEnd() is true, the ending neighbor node
		//gets added to exploredNodes, and returns out of the method.
		//Also, it ignores the first node entered into exploredNodes.
		if(exploredNodes.size() > 0 && checkForNeighboringEnd(inputNode))
		{
			return;
		}
		for(int i=0;i<inputNode.neighboringNodes.size();i++)
		{
			for(int j=0;j<network.NodeList.size();j++)
			{
				//if(inputNode.neighboringNodes.get(i) == network.NodeList.get(j).Name) 
				//If inputNode matches the name of a node in the network and if the end entrance is not in the exploredNodes Arraylist
				if(inputNode.neighboringNodes.get(i).equals(network.NodeList.get(j).Name) && !exploredNodes.get(exploredNodes.size()-1).contains(endentrances))
				{
					depthFirstSearchComplete(network.NodeList.get(j));
					//numNodesToRemove = checkNeighbors(inputNode.neighboringNodes);
				}
				/*
				else if(numNodesToRemove > 0)
				{
					exploredNodes.remove(exploredNodes.size() - 1);
					numNodesToRemove--;
					return;
				}
				*/

			}
		}
	}
	
	
	
	
	boolean checkForNeighboringEnd(Node input)
	{
		//boolean found = false;
		
		for(int i = 0; i < input.neighboringNodes.size(); i++)
		    for(int j = 0; j < endentrances.size(); j++)
			    if(input.neighboringNodes.get(i).equals(endentrances.get(j)))
			    {
			    	for(int k = 0; k < network.NodeList.size(); k++)
			    		if(input.neighboringNodes.get(i).equals(network.NodeList.get(k).Name))
			    		{
			    			exploredNodes.add(network.NodeList.get(k));
			    			return true;
			    		}
			    }
				
		
		return false;
	}
	
	
	
	//Isn't working how I would have hoped it would have worked...
	//Attempting to find a neighbor of inputNode from depthFirstSearchComplete()
	//earlier in the exploredNodes list and delete nodes in the list to the index
	//of the already inserted neighbor, to then insert the current inputNode, thereby
	//redirecting the graph and eliminating unneeded side trips...
	int checkNeighbors(ArrayList<String> neighbors)
	{
		boolean found = false;
		int index = 0;
		
		for(int i = 0; i < neighbors.size() && !found; i++)
		{
			for(int j = 0; j < exploredNodes.size() && !found; j++)
			{
				if(neighbors.get(i).equals(exploredNodes.get(j).Name) && j != exploredNodes.size() - 1)
				{
						index = exploredNodes.size() - j;
						found = true;
				}
			}
		}
		
		return index;
	}
	
	
	
	
	ArrayList<String> getEntranceList(int pos)
	{
		ArrayList<String> entrances = new ArrayList<String>();
		
		switch(pos)
		{
			case 0:  entrances.add("facilities_1");  break; //Anthropology
			case 1:  entrances.add("art_1");  //Art Buildings
					 entrances.add("art_2");
					 break;
			case 2:  entrances.add("bouj_1");  break; //Beaujolais
			case 3:  entrances.add("path_306");  break; //Boiler Plant
			case 4:  entrances.add("facilities_1");  break; //Building 49
			case 5:  entrances.add("cab_1");  //Cabernet Village
					 entrances.add("cab_2");
					 break;
			case 6:  entrances.add("carson_1");  //Carson Hall
			         entrances.add("carson_2");
			         entrances.add("carson_3");
			         break;
			case 7:  entrances.add("childrens_school_1");  //Children's School
					 break;
			case 8:  entrances.add("commons_1");  //Commons
			         entrances.add("commons_2");
			         break;
			case 9:  entrances.add("coop_1");  break;  //Cooperage 
			case 10:  entrances.add("darwin_1");  //Darwin Hall
			          entrances.add("darwin_2");
					  entrances.add("darwin_3");
					  break; 
			case 11:  entrances.add("environment");  //Environmental Tech Center
			          break;
			case 12:  entrances.add("person_1");  break;  //Person Theatre
			case 13:  entrances.add("facilities_1");  break;  //Facilities
			case 14:  entrances.add("field_house_1");  break;  //FieldHouse
			case 15:  entrances.add("gmc_1");  break;  //Green Music Center
			case 16:  entrances.add("pe_1");   //Gymnasium
			          entrances.add("pool_1");
					  break;  
			case 17:  entrances.add("parking_info_1");  break;  //Info Booth North
			case 18:  entrances.add("path_303");  
			          entrances.add("path_305");  
					  entrances.add("path_302");  
					  entrances.add("path_300");
					  entrances.add("path_310");  					  
					  break;  //Info Booth South
			case 19:  entrances.add("ives_1");  //Ives Hall
					  entrances.add("ives_3");
					  break;  
			case 20:  entrances.add("path_150");  
					  entrances.add("path_221");
					  break;  //Lakes
			case 21:  entrances.add("nichols_1");    //Nichols Hall
					  break;
			case 22:  entrances.add("bouj_1");  break;  //Observatory
			case 23:  entrances.add("path_57");    //Parking Lot A
					  entrances.add("path_56");
					  entrances.add("path_91");
					  entrances.add("path_92");
					  entrances.add("path_93");
					  break;
			case 24:  entrances.add("path_368");  //Parking Lot C, D
			          entrances.add("path_370");
					  entrances.add("path_273");
					  entrances.add("path_265");
			          break;  
			case 25:  entrances.add("path_207");  //Parking Lot E
			          entrances.add("path_206");
					  entrances.add("path_346");
					  entrances.add("path_316");
					  entrances.add("path_315");
					  break;  
			case 26:  entrances.add("tuscany_1");  break;  //Parking Lot F
			case 27:  entrances.add("path_169");  //Parking Lot G
                      entrances.add("path_172");
					  break;  
			case 28:  entrances.add("path_167");  //Parking Lot H
			          entrances.add("path_165");
					  break;  
			case 29:  entrances.add("path_291");
					  entrances.add("path_285");
					  entrances.add("path_282");
					  break;  //Parking Lot J
			case 30:  entrances.add("path_183");  //Parking Lot Juniper Lane
                      entrances.add("path_185");
					  break;  
			case 31:  entrances.add("path_158");  break;  //Parking Lots L, M, N, O
			case 32:  entrances.add("path_264");  break;  //Police and Parking Services
			case 33:  entrances.add("rec_1");  break;  //Recreation Center
			case 34:  entrances.add("salazar_1");  //Salazar Hall
			          entrances.add("salazar_2");
					  entrances.add("salazar_3");
					  break;  
			case 35:  entrances.add("sauv_1");  //Sauvignon Village
			          entrances.add("sauv_4");
					  entrances.add("sauv_5");
					  entrances.add("sauv_3");
					  entrances.add("sauv_2");
					  break;  
			case 36:  entrances.add("library_2");  //Schultz Info Center
			          entrances.add("library_1");
			          break;  
			case 37:  entrances.add("path_175");  break;  //Seawolf Shops
			case 38:  entrances.add("tuscany_1");  break;  //Seawolf Stadium
			case 39:  entrances.add("stevenson_1");    //Stevenson Hall
					  entrances.add("stevenson_2");
					  entrances.add("stevenson_3");
					  entrances.add("stevenson_4");
					  break;
			case 40:  entrances.add("path_368");  //Student Center
			          entrances.add("path_329");
					  break;  
			case 41:  entrances.add("health_1");   //Student Health Center
					  entrances.add("health_2");
					  break;
			case 42:  entrances.add("su_1");  //Student Union
					  entrances.add("pub_1");
					  break;
			case 43:  entrances.add("path_245");  
					  entrances.add("path_253");
					  break; //Toast
			case 44:  entrances.add("tuscany_1");  break; //Tuscany Village
			case 45:  entrances.add("verdot_1");  break; //Verdot Village
			case 46:  entrances.add("zin_1");           //Zinfandel Village
					  entrances.add("zin_2");
					  entrances.add("zin_3");
					  entrances.add("zin_4");
					  entrances.add("zin_5");
					  entrances.add("zin_6");
					  entrances.add("zin_7");
					  entrances.add("zin_8");
			          break;
			//case 47:  entrances.add("not available");  break;
			default:  entrances.add("not available");  break;
		}
		
		return entrances;
	}



}
