/*
Chris Holland
July 19, 2018
*/

/* BaseballElimination.java
   CSC 226 - Summer 2018
   Assignment 4 - Baseball Elimination Program
   
   This template includes some testing code to help verify the implementation.
   To interactively provide test inputs, run the program with
	java BaseballElimination
	
   To conveniently test the algorithm with a large input, create a text file
   containing one or more test divisions (in the format described below) and run
   the program with
	java -cp .;algs4.jar BaseballElimination file.txt (Windows)
   or
    java -cp .:algs4.jar BaseballElimination file.txt (Linux or Mac)
   where file.txt is replaced by the name of the text file.
   
   The input consists of an integer representing the number of teams in the division and then
   for each team, the team name (no whitespace), number of wins, number of losses, and a list
   of integers represnting the number of games remaining against each team (in order from the first
   team to the last). That is, the text file looks like:
   
	<number of teams in division>
	<team1_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>
	...
	<teamn_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>

	
   An input file can contain an unlimited number of divisions but all team names are unique, i.e.
   no team can be in more than one division.


   R. Little - 07/13/2018
*/

import edu.princeton.cs.algs4.*;
import java.util.*;
import java.io.File;

//Do not change the name of the BaseballElimination class
public class BaseballElimination{
	
	// We use an ArrayList to keep track of the eliminated teams.
	public ArrayList<String> eliminated = new ArrayList<String>();

	/* BaseballElimination(s)
		Given an input stream connected to a collection of baseball division
		standings we determine for each division which teams have been eliminated 
		from the playoffs. For each team in each division we create a flow network
		and determine the maxflow in that network. If the maxflow exceeds the number
		of inter-divisional games between all other teams in the division, the current
		team is eliminated.
	*/
	public BaseballElimination(Scanner s){
		/*
		* Part 1:
		* Read all values from the scanner and store them in
		* their appropriate places
		*/
		int n = s.nextInt();
		int[] wins = new int[n];
		int[] remaining = new int[n];
		int[][] games =  new int[n][n];
		String[] teams = new String[n];
		HashMap<String, Integer> ids = new HashMap<String, Integer>();
		
		for (int i = 0; i < n; i++) {
			teams[i] = s.next();
			ids.put(teams[i], i);
			wins[i] = s.nextInt();
			remaining[i] = s.nextInt();
			
			for (int j = 0; j < n; j++) {
				games[i][j] = s.nextInt();
			}
		}
		/*
		* End of Part 1
		*/
		
		/*
		* Part 2:
		* Build a flow network for each team and
		* run FordFulkerson on them to determine whether 
		* they are eliminated
		*/	
		for (HashMap.Entry<String, Integer> entry : ids.entrySet()) {
			String curr = entry.getKey();
			int pos = entry.getValue();
			boolean current_is_out = false;
			int outflow = 0;
			
			/*
			* Trivial eliminations, sets a flag
			* and exits the iteration early
			*/
			for (int i = 0; i < n; i++) {
				if (i != pos && wins[pos] + remaining[pos] < wins[i]) {
					eliminated.add(curr);
					current_is_out = true;
					break;
				}
			}
			
			if (current_is_out) continue;
				
			int vertices = n + (n-1)*(n-2)/2 + 2;
			FlowNetwork network = new FlowNetwork(vertices);
			
			for (int i = 0, v = n; i < n; i++) {
				if (i == pos) continue;
				for (int j = i+1; j < n; j++) {
					if (j == pos) continue;
					
					outflow += games[i][j];
					
					FlowEdge edge1 = new FlowEdge(vertices-2, v, games[i][j]);
					network.addEdge(edge1);
					
					FlowEdge edge2 = new FlowEdge(v, i, Double.POSITIVE_INFINITY);
					network.addEdge(edge2);
				
					FlowEdge edge3 = new FlowEdge(v, j, Double.POSITIVE_INFINITY);
					network.addEdge(edge3);
					
					v++;
				}
			}
			
			for (int i = 0; i < n; i++) {
				if (i != pos) {
					int capacity = wins[pos] + remaining[pos] - wins[i];
					FlowEdge edge = new FlowEdge(i, vertices-1, capacity);
					network.addEdge(edge);
				}
			}
			
			//System.out.println(network.toString());
			
			FordFulkerson flow = new FordFulkerson(network, vertices-2, vertices-1);
			
			if (flow.value() != outflow) 
				eliminated.add(curr);
			
		}
	}
		
	/* main()
	   Contains code to test the BaseballElimantion function. You may modify the
	   testing code if needed, but nothing in this function will be considered
	   during marking, and the testing process used for marking will not
	   execute any of the code below.
	*/
	public static void main(String[] args){
		Scanner s;
		if (args.length > 0){
			try{
				s = new Scanner(new File(args[0]));
			} catch(java.io.FileNotFoundException e){
				System.out.printf("Unable to open %s\n",args[0]);
				return;
			}
			System.out.printf("Reading input values from %s.\n",args[0]);
		}else{
			s = new Scanner(System.in);
			System.out.printf("Reading input values from stdin.\n");
		}
		
		BaseballElimination be = new BaseballElimination(s);		
		
		if (be.eliminated.size() == 0)
			System.out.println("No teams have been eliminated.");
		else
			System.out.println("Teams eliminated: " + be.eliminated);
	}
}
