import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;

public class Sim {
	public static void main(String[] args) {
		Arena arena = new Arena();
		
		//arena.print();
	}
	
}
class Arena {
	ArrayList<Country> countries = new ArrayList<Country>();
	ArrayList<ArrayList<Country>> countryArchive = new ArrayList<ArrayList<Country>>();
	ArrayList<ArrayList<Country>> reproducingCountries = new ArrayList<ArrayList<Country>>();
	int numCountries;
	int GDP;
	int numGames;
	int iters;
	double reproductionFactor;
	Random r = new Random();
	Coinflipper flipper;
	Faceoff faceoff;
	ArrayList<int[]> memory = new ArrayList<int[]>();
	CountryScanner cs;
	double randomFactor;
	public Arena(ArrayList<Country> c) {
		countries = c;
	}
	public Arena() {
		GDP = 10000;
		numCountries = 100;
		iters = 10000;
		reproductionFactor = 0.2;
		randomFactor = 0.2;
		cs = new CountryScanner();
		flipper = new Coinflipper();
		faceoff = new Faceoff(10,cs.askForPayouts());
		populate();
		play();
		Writer w = new Writer();
		try {
			w.Write("Results.csv", memory);
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(true) {
			cs.askForCountryID();
			cs.findCountry(countryArchive);
		}
		
	}
	public void askForCountry() {
		
	}
	public void play() {
		for(int k = 0; k<iters; k++) {
			int[] data = new int[30];
			data[0] = k;
			for(int i = 0; i < countries.size(); i++) {
				for (int j = i; j < countries.size(); j++ ) {
					if (j != i) {
						faceoff.start(countries.get(i), countries.get(j));
						data[1] = data[1] + faceoff.c;
						data[2] = data[2] + faceoff.d;
					}
				}
			}
//			industrialize();
			rememberGains();
			rank();
			int[] s = stats(k);
			for(int i = 0;i<s.length;i++) {
				data[i+3] = s[i];
			}
			memory.add(data);

			//System.out.println("Iteration" + k);
			//countries.get(0).print();			
			distributeWinnings();
			evolve();
			ArrayList<Country> archive = new ArrayList<Country>();
			for(int i = 0;i<countries.size();i++) {
				archive.add(countries.get(i).copy());
			}
			countryArchive.add(archive);
			reset();
		}
	}
	public void reset() {
		for (int i = 0; i<countries.size(); i++) {
			countries.get(i).resetGains();
			countries.get(i).resetStats();
		}
	}
	public void distributeWinnings() {
		for (int i = 0; i<countries.size(); i++) {
			countries.get(i).useGains();
		}
	}
	public void industrialize() {
		for (int i = 0;i<countries.size();i++) {
			countries.get(i).useIndustry();
		}
	}
	public int[] stats(int k) {
		int[] s = new int[27];
		int sum = 0;
		int median = 0;
		int mean = 0;
		int MC = 0;
		int SC = 0;
		int SD = 0;
		int MD = 0;
		int esc = 0;
		int forg = 0;
		int und = 0;
		int all = 0;
		int wD = 0;
		double gD = 0;
		double gS = 0;
		int topCoops = 0;
		int coopFirsts = 0;
		int topCoopFirsts = 0;
		double repRatios = 0;
		double sumRatios = 0;
		int warFarers = 0;
		int resolutes = 0;
		int[] allBubbles = new int[2];
		int[] topBubbles = new int[2];
		int[] repBubbles = new int[2];
		int repFirstCoops = 0;
		for(int i = 0; i<countries.size(); i++) {
			countries.get(i).findFlows();
			countries.get(i).buyAndSellGold();
			if(i<(int)countries.size()/10) {
				topCoops = topCoops + countries.get(i).mutualCooperations + countries.get(i).soloCooperations;
				if(countries.get(i).cooperateFirst()) {
					topCoopFirsts++;
				}
				topBubbles[0] = topBubbles[0] + countries.get(i).bubbles()[0];
				topBubbles[1] = topBubbles[1] + countries.get(i).bubbles()[1];
			}
			sum = sum + countries.get(i).getGains();
			MC = MC + countries.get(i).mutualCooperations;
			SC = SC + countries.get(i).soloCooperations;
			SD = SD + countries.get(i).soloDefections;
			MD = MD + countries.get(i).mutualDefections;
			gD = gD + countries.get(i).goldDemand;
			gS = gS + countries.get(i).goldSupply;
			esc = esc + countries.get(i).escalations;
			forg = forg + countries.get(i).forgivals;
			und = und + countries.get(i).undercuts;
			all = all + countries.get(i).alliances;
			wD = wD + countries.get(i).warDeclarations;
			sumRatios = sumRatios + 10*countries.get(i).getCoopRatio();
			allBubbles[0] = allBubbles[0] + countries.get(i).bubbles()[0];
			allBubbles[1] = allBubbles[1] + countries.get(i).bubbles()[1];
			if(countries.get(i).bubbles()[0] == 0) {
				warFarers++;
			}
			if(countries.get(i).bubbles()[1] == 0) {
				resolutes++;
			}
			if(countries.get(i).cooperateFirst()) {
				coopFirsts++;
			}
			if(i == ((int) countries.size()/2)) {
				median = countries.get(i).getGains();
			}
		}
		if(k>0) {
			for(int i = 0;i<reproducingCountries.get(k-1).size();i++) {
				repBubbles[0] = repBubbles[0] + reproducingCountries.get(k-1).get(i).bubbles()[0];
				repBubbles[1] = repBubbles[1] + reproducingCountries.get(k-1).get(i).bubbles()[1];
				repRatios = repRatios + reproducingCountries.get(k-1).get(i).getCoopRatio();
				if(reproducingCountries.get(k-1).get(i).cooperateFirst()) {
					repFirstCoops++;
				}
			}	
			repRatios = repRatios/((double)reproducingCountries.get(k-1).size());
			s[14] = (int) ((double)repBubbles[0]/((double)reproducingCountries.get(k-1).size())*10.0);
			s[15] = (int) ((double)repBubbles[1]/((double)reproducingCountries.get(k-1).size())*10.0);
			s[16] = (int) (repRatios*100);
			s[17] = (int) ((double)repFirstCoops/((double)reproducingCountries.get(k-1).size())*10.0);
		}
		
		mean = (int) sum/countries.size();
		s[0] = mean;
		s[1] = median;
		s[2] = MC;
		s[3] = SC;
		s[4] = SD;
		s[5] = MD;
		s[6] = (int) sumRatios/countries.size();
		s[7] = topCoops;
		s[8] = coopFirsts;
		s[9] = topCoopFirsts;
		s[10] = allBubbles[0];
		s[11] = allBubbles[1];
		s[12] = topBubbles[0];
		s[13] = topBubbles[1];
		s[18] = (int)gD;
		s[19] = (int)gS;
		s[20] = esc;
		s[21] = forg;
		s[22] = und;
		s[23] = all;
		s[24] = wD;
		s[25] = resolutes;
		s[26] = warFarers;
 		return s;
	}
//	public void think() {
//		for(int i = 0;i<countries.size();i++) {
//			countries.get(i).setStrategy(countries.get(i).findBestStrategy(5, countries));
//		}
//	}
	public void evolve() {
		ArrayList<Country> reproduced = new ArrayList<Country>();
		for(int i = 0;i<countries.size();i++) {
			if(flipper.flip((int)(Math.pow((double)(countries.size()-i),0.5)/reproductionFactor))) {
				int j = 0;
				while(j<countries.size()) {
					if (!flipper.flip(4)) {
						break;
					}
					j++;

				}
				reproduced.add(countries.get(j).copy());
//				System.out.println((int) Math.pow(2.0/countries.get(j).getCoopRatio(), 0.5));
//
				countries.get(i).setStrategy(countries.get(j).strategy.copy());
				countries.get(i).adjustStrategy(2);
			}
//			else {
//				if(flipper.flip((int)(1.0/randomFactor))) {
//					countries.get(i).adjustStrategy(1);
//				}
//			}
		}
		reproducingCountries.add(reproduced);

	}
	public void rememberGains() {
		for(int i = 0;i<countries.size();i++) {
			countries.get(i).rememberGains();
		}
	}
	public void rank() {
		for(int i = 0; i< countries.size();i++) {
			countries.get(i).setPrevRanking(i);
		}
		countries.sort(Comparator.comparing(Country::avgRecentGains).reversed());
		for(int i = 0; i< countries.size();i++) {
			countries.get(i).setRank(i);
		}
	}
	public void populate() {
		boolean coop = true;
		for(int i = 0; i<numCountries; i++) {
			Country c = new Country(i, GDP);
			c.strategy.Matrix.getFirst().set(coop);
			coop = !coop;
			if(flipper.flip(3)) {
				c.adjustStrategy(1);
			}
			countries.add(c);
		}
	}
	public void print() {
		for(int i = 0; i<countries.size(); i++) {
			countries.get(i).print();
		}
	}
	
}
class Country {
	int GDP = 0;
	int gains = 0;
	ArrayList<Integer> recentGains = new ArrayList<Integer>();
	int ranking = 0;
	int prevRanking = 0;
	int soloCooperations = 0;
	int mutualCooperations = 0;
	int mutualDefections = 0;
	int soloDefections = 0;
	int industry = 0;
	int baseIndustry = 0;
	double sensitivity = 0.2;
	int escalations = 0;
	int forgivals = 0;
	int undercuts = 0;
	int alliances = 0;
	int warDeclarations = 0;
	int resources;
	int ID;
	boolean onlyDefect;
	boolean onlyCoop;
	double goldDemand;
	double goldSupply;
	ArrayList<Country> otherCountries= new ArrayList<Country>();
	Strategy strategy = new Strategy(ID);
	String name;
	
	ArrayList<int[]> memory = new ArrayList<int[]>();
		public Country() {
	}
	public Country(int n, int gdp) {
		ID = n;
		GDP = gdp;
	}
	public Country(int i) {
		ID = i;
	}
	public Country (int i, int gdp, Strategy s) {
		ID = i;
		GDP = gdp;
		strategy = s;
	}
	public Country(String n, int gdp, int r) {
		name = n;
		GDP = gdp;
		resources = r;
	}
	public Country(int i, int gdp, Strategy s, int r, int sC, int mC, int mD, int sD, double gD, double gS, int esc, int forg, int und, int all) {
		ID = i;
		GDP = gdp;
		strategy = s;
		ranking = r;
		mutualCooperations = mC;
		mutualDefections = mD;
		soloCooperations = sC;
		soloDefections = sD;
		goldDemand = gD;
		goldSupply = gS;
		int escalations = esc;
		int forgivals = forg;
		int undercuts = und;
		int alliances = all;
	}
	public void setStrategy(Strategy s) {
		strategy = s;
	}
	public void learnOtherCountry(int id) {
		for(int i = otherCountries.size();i<=id;i++) {
			otherCountries.add(new Country(i));
		}
	}
//	public void learnStrategy(int id, Bubble b) {
//		otherCountries.get(id).strategy.addBubble(id, b);
//	}
	public void determineStrategy(Bubble b) {
		
	}
	public void setRank(int r) {
		ranking = r;
	}
	public void setPrevRanking(int r) {
		prevRanking = r;
	}
	public void setGDP(int g) {
		GDP = g;
	}
	public void changeGDP(int g) {
		GDP = GDP + g;
	}
	public int getGDP() {
		return GDP;
	}
	public void addGains(int pG) {
		gains = gains + pG;
	}
	public int getGains() {
		return gains;
	}
	public void useAndResetAll() {
		useAndResetGains();
		resetStats();
	}
	public void resetAll() {
		resetGains();
		resetStats();
	}
	public void useAndResetGains() {
		useGains();
		resetGains();
	}
	public void useGains() {
		GDP = GDP + gains;
	}
	public void resetGains() {
		gains = 0;
	}
	public void rememberGains() {
		recentGains.add(gains);
		if(recentGains.size()>(int)(1.0/sensitivity)) {
			recentGains.removeFirst();
		}
	}
	public int avgRecentGains() {
		double sum =0;
		for(int i = 0;i<recentGains.size();i++) {
			sum = sum + recentGains.get(i);
		}
		return (int) sum/recentGains.size();
	}
	public void resetStats() {
		soloCooperations = 0;
		mutualCooperations = 0;
		mutualDefections = 0;
		soloDefections = 0;
		goldDemand = 0;
		goldSupply = 0;
	}
	public int compareRanks() {
		return prevRanking - ranking;
	}
	public void changeIndustry(int i) {
		industry = industry + i;
	}
	public int getTotalIndustry() {
		return industry + baseIndustry;
	}
	public void useIndustry() {
		gains = gains + getTotalIndustry();
	}
	public void adjustStrategy(int iters) {
		for(int i = 0; i<iters;i++) {
			strategy.adjust();
		}
	}
	public boolean cooperateFirst() {
		return strategy.Matrix.getFirst().cooperate;
	}
	public int[] bubbles() {
		return strategy.allBubbles();
	}
	public void findFlows() {
		int[] f = strategy.flowStats();
		escalations = f[0];
		forgivals = f[1];
		undercuts = f[2];
		alliances = f[3];
		warDeclarations = f[4];
	}
	public Strategy findBestStrategy(int strats, ArrayList<Country> countries) {
		Faceoff f = new Faceoff(10);
		int max = -100000;
		Strategy bestStrat = new Strategy();
		for (int i = 0; i< strats;i++) {
			Country c = this.copy();
			c.adjustStrategy(1);
			f.start(c, countries.get(i).copy());
			if(c.getGains()>max) {
				max = c.getGains();
				bestStrat = c.strategy;
			}
		}
		return bestStrat;
	}
	public Country copy() {
		return new Country(ID, GDP,strategy.copy(), ranking, soloCooperations, mutualCooperations, mutualDefections, soloDefections, goldDemand, goldSupply, escalations, forgivals, undercuts, alliances);
		
	}
	public void addMC(int id) {
		mutualCooperations++;
	}
	public void addSC(int id) {
		soloCooperations++;
	}
	public void addMD(int id) {
		mutualDefections++;
	}
	public void addSD(int id) {
		soloDefections++;
	}
	public int getCs()	{
		return mutualCooperations + soloCooperations;
	}
	public int getDs() {
		return mutualDefections + soloDefections;
	}
	public int getDsAndCs(){
		return getCs() + getDs(); 
	}
	public void buyAndSellGold() {
		goldDemand = ((double)soloDefections)*(1-plannedCoopRatio());
		goldSupply = ((double)mutualDefections)*plannedCoopRatio();

	}
	public double plannedCoopRatio(){
		return strategy.bubbleRatio();
	}
	public double coopRatio() {
		return ((double) getCs())/((double) getDsAndCs());
	}
	public double getCoopRatio() {
		if(coopRatio()<0.01) {
			return 0.01;
		}
		return coopRatio();
	}
	public void write(int[] o){
		int [] mem = {o[0],o[1],GDP,ranking};
		memory.add(mem);
	}
	public void toFile() {
		Writer w = new Writer();
		try {
			w.Write(name, memory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void print() {
		System.out.println("Country: " + ID);
		System.out.println("GDP: " + GDP);
		System.out.println("Rank: " + ranking);
		System.out.println("Industry: " + getTotalIndustry());
		System.out.println("Solo Cooperations:" + Integer.toString(soloCooperations) + "|Mutual Cooperations:" + Integer.toString(mutualCooperations) + "|Solo Defections:" + Integer.toString(soloDefections) + "|Mutual Defections:" + Integer.toString(mutualDefections));
		strategy.print();
		System.out.println();
	}
	
}
class Strategy {
	ArrayList<Bubble> Matrix;
	Random r;
	Coinflipper flipper;
	int CountryID;
	public Strategy() {
		Matrix = new ArrayList<Bubble>();
		r = new Random();
		flipper = new Coinflipper(); 
		initialize();
	}
	public Strategy(int c) {
		CountryID = c;
		Matrix = new ArrayList<Bubble>();
		r = new Random();
		flipper = new Coinflipper(); 
		initialize();
	}
	public Strategy(ArrayList<Bubble> m) {
		r = new Random();
		flipper = new Coinflipper(); 
		Matrix = m;
	}
	public void initialize() {																							//Initial strategic matrix
		Bubble b1 = new Bubble(0, CountryID);
		Bubble b2 = new Bubble(1, CountryID);
		b1.set(true);
		b1.setC(b1);
		b1.setD(b2);
		b2.set(false);
		b2.setC(b1);
		b2.setD(b2);
		Matrix.add(b1);
		Matrix.add(b2);
	}
	public void think() {
		
	}
	public void adjust() {																								//Randomly change one value on strategy matrix, either decision, flow, or bubbles
		int rChoice = 0;
		int rInd = r.nextInt(Matrix.size());
		int rInds = r.nextInt(Matrix.size()-1);
		int rVal = r.nextInt(Matrix.size());
		int rVal2 = r.nextInt(Matrix.size());
		while(true) {
			rChoice = r.nextInt(5);
			if(!((Matrix.size()==2 && rChoice == 4)||(Matrix.size()>=10 && rChoice == 3))) {
				break;
			}
		}
		if (rChoice == 0) {
			changeBubbles(rInd);
		}else if(rChoice == 1) {
			while(rVal == Matrix.get(rInd).ifC.ID) {																	//Ensuring that Matrix is changed
				rVal = r.nextInt(Matrix.size());
			}
			changeCFlow(rInd, rVal);
		}else if(rChoice == 2) {
			while(rVal == Matrix.get(rInd).ifD.ID) {
				rVal = r.nextInt(Matrix.size());
			}
			changeDFlow(rInd, rVal);
		}else if(rChoice == 3) {
			addBubble(flipper.flip(2), Matrix.get(rVal), Matrix.get(rVal2));
			if(flipper.flip(2)) {																						//Add random connection to new bubble
				Matrix.get(rInds).setC(Matrix.getLast());;
			}else {
				Matrix.get(rInds).setD(Matrix.getLast());;
			}
		}else if(rChoice == 4){
			removeLastBubble();
		}
		trim();
	}
	public ArrayList<Bubble> cycle(Bubble b,ArrayList<Bubble> done) {

		if (!done.contains(b)) {
			done.add(b);
			cycle(b.ifC, done);
			cycle(b.ifD, done);
		}
		return done;

	}
	public void trim() {
		ArrayList<Bubble> done = cycle(Matrix.getFirst(), new ArrayList<Bubble>());
		if(done.size()>1) {
			done.sort(Comparator.comparing(Bubble::getID));
			for (int i = 0; i<done.size();i++) {
				done.get(i).setID(i);
			}
			Matrix = done;
		}
	}
	public void changeBubbles(int iB) {
		Matrix.get(iB).flipCoop();
	}
	public void changeCFlow(int iCF, int flowC) {
		Matrix.get(iCF).setC(Matrix.get(flowC));
	}
	public void changeDFlow(int iDF, int flowD) {
		Matrix.get(iDF).setD(Matrix.get(flowD));
	}
	public void addBubble(Bubble b) {
		for(int i = Matrix.size();i<=b.ID;i++) {
			Matrix.add(new Bubble(i));
		}
		Matrix.add(b);
	}
	public void addBubble(boolean coop, int C, int D) {
		Bubble b = new Bubble(Matrix.size(), CountryID);
		Matrix.add(b);
		b.set(coop);
		b.setC(Matrix.get(C));
		b.setD(Matrix.get(D));
	}
	public void addBubble(boolean coop, Bubble C, Bubble D) {
		Bubble b = new Bubble(Matrix.size(), CountryID, coop, C, D);
		Matrix.add(b);
	}
	public void removeLastBubble() {
		for (int i = 0; i<Matrix.size(); i++) {																			//ensures no floating connections
			if((Matrix.get(i).ifC.ID)== Matrix.size()-1) {
				changeCFlow(i, r.nextInt(Matrix.size()-1));
			}
			if((Matrix.get(i).ifD.ID)== Matrix.size()-1) {
				changeDFlow(i, r.nextInt(Matrix.size()-1));
			}
		}
		Matrix.removeLast();
		
	}

	public double bubbleRatio() {
		int[] aB = allBubbles();
		return (((double) aB[0])/((double) (aB[1] + aB[0])));
	}
	public int[] allBubbles() {
		int[] d = {0,0};
		for (int i = 0; i<Matrix.size(); i++) {
			if(Matrix.get(i).cooperate) {
				d[0]++;
			}else {
				d[1]++;

			}
		}
		return d;
	}
	public int[] flowStats() {
		int[] stats = new int[5];
		for (int i = 0; i<Matrix.size();i++) {
			if(Matrix.get(i).ifC.cooperate) {
				stats[3]++;
			}else {
				stats[2]++;
			}
			if(Matrix.get(i).ifD.cooperate) {
				stats[1]++;
			}else {
				stats[0]++;
			}
			if(Matrix.get(i).ifD == Matrix.get(i) && (!Matrix.get(i).cooperate)) {
				stats[4]++;
			}
		}
		return stats;
	}
	public Strategy copy() {
		ArrayList<Bubble> c = new ArrayList<Bubble>();
		for (int i = 0; i < Matrix.size(); i++) {
			Bubble toC = Matrix.get(i);
			Bubble b = toC.copy();
			c.add(b);
		}
		for (int i = 0; i < c.size(); i++) {
			Bubble b = c.get(i);
			b.setC(c.get(Matrix.get(i).ifC.ID));
			b.setD(c.get(Matrix.get(i).ifD.ID));
		}
		Strategy s = new Strategy(c);
		return s;
	}
	public void print() {
		System.out.println("Strategy:");
		for(int i = 0;i<Matrix.size();i++) {
			Matrix.get(i).print();
		}
	}
	
}
class Bubble {
	boolean cooperate;
	int countryID;
	int ID;
	Bubble ifC;
	Bubble ifD;
	Bubble prevBubble;
	public Bubble(int id) {
		ID = id;
	}
	public Bubble(int id, int cID) {
		ID = id;
		countryID = cID;
	}
	public Bubble(int id, boolean coop) {
		ID = id;
		cooperate = coop;
	}
	public Bubble(int id, int c, boolean coop, Bubble C, Bubble D) {
		ID = id;
		countryID = c;
		cooperate = coop;
		ifC = C;
		ifD = D;
	}

	public Bubble(int id, int cID, boolean coop, Bubble C, Bubble D, Bubble prevBubble) {
		ID = id;
		cooperate = coop;
		ifC = C;
		ifD = D;
	}
	public Bubble copy() {
		Bubble c = new Bubble(ID, cooperate);
		return c;
	}
	public void setID(int i) {
		ID = i;
	}
	public int getID() {
		return ID;
	}
	public void flipCoop() {
		cooperate = !cooperate;
	}
	public void setC(Bubble C) {
		ifC = C;
	}
	public void setD(Bubble D) {
		ifD = D;
	}
	public void setPrevBubble(Bubble prev) {
		prevBubble = prev;
	}
	public void set(boolean coop) {
		cooperate = coop;
	}
	public void print() {
		String s = "| ID: ";
		s = s + Integer.toString(ID);;
		if(cooperate) {
			s = s + "| Cooperate ";
		}else {
			s = s + "| Defect    ";
		}
		s = s + "| If Cooperate: ";
		s = s + Integer.toString(ifC.ID);
		s = s + " | If Defect: ";
		s = s + Integer.toString(ifD.ID);
		s = s + " |";
		System.out.println(s);
	}
}
class Coinflipper {
	Random r = new Random();
	public boolean flip(int chance) {
		if (r.nextInt(chance) == 0) {
			return true;
		}
		return false;
	}
}
class Writer {
	
    public Writer() {
    }
    public void Write(String filename, ArrayList<int[]> arr) throws IOException {
    	
        String csvFile = filename;
        try (FileWriter fw = new FileWriter(csvFile);
                PrintWriter pw = new PrintWriter(fw)) {
        	for(int i = 0;i<arr.size();i++) {
        		int[] a = arr.get(i);
        		String s = "";
        		for(int j = 0;j<a.length-1;j++) {
        			s = s + Integer.toString(a[j])+",";
        		}
        		s = s + Integer.toString(a[a.length-1]);
        		pw.println(s);
        	}
        }catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }
    public void Write(String filename, int[] arr) throws IOException {
    	
        String csvFile = filename;
        try (FileWriter fw = new FileWriter(csvFile);
                PrintWriter pw = new PrintWriter(fw)) {
        	
        }catch (IOException e) {
            System.err.println("Error writing to CSV file: " + e.getMessage());
        }
    }

}
class CountryScanner {

    private static Scanner scanner = new Scanner(System.in);
    
    // This variable will store the selected country ID
    private static int selectedCountryID = -1;
    private static int iteration = 0;
    private static ArrayList<ArrayList<Country>> countryArchive = new ArrayList<ArrayList<Country>>();
    public CountryScanner(ArrayList<ArrayList<Country>> cA) {
        countryArchive = cA;
    }
    public CountryScanner() {
    	
    }
    public int[] askForPayouts() {
    	System.out.println("Input Payout Matrix in order MC SC SD MD (Ex. 20 0 30 -20):");
    	int [] payouts = new int[4];
    	while (true) {
            try {
            	payouts[0] = scanner.nextInt();
            	payouts[1] = scanner.nextInt();
            	payouts[2] = scanner.nextInt();
            	payouts[3] = scanner.nextInt();
            	scanner.nextLine();
            	System.out.println(Arrays.toString(payouts));
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid numeric iteration: ");
            }
        }
    	return payouts;
    }
    public void askForCountryID() {
        System.out.print("Enter the iteration you want to select: ");
        
        while (true) {
            try {
                iteration = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid numeric iteration: ");
            }
        }
        System.out.print("Enter the rank you want to select: ");
        
        while (true) {
            try {
                selectedCountryID = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.print("Invalid input. Please enter a valid numeric rank: ");
            }
        }
        
    }
    public static void findCountry(ArrayList<ArrayList<Country>> ca) {
    	ca.get(iteration).get(selectedCountryID).print();
    }
    

}
class Faceoff {
	int games;
	int currGame;
	Country ctryA;
	Country ctryB;
	int aID;
	int bID;
	int d;
	int c;
	int[] iPayout = {0,-0};
	int[] cPayout = {20, 0};													//Payouts if subject country cooperates. If the other country cooperates, sizable fixed reward for both. If the other defects, penalization based on cooperating countries GDP
	int[] dPayout = {30, -20};												//Payouts if subject country defects. If the other country cooperates, reward based on cooperating countries GDP. If the other defects, small penalty for both.
	public Faceoff(int g) {
		games = g;
	}
	public Faceoff(int g, int[] payouts) {
		games = g;
		cPayout[0] = payouts[0];
		cPayout[1] = payouts[1];
		dPayout[0] = payouts[2];
		dPayout[1] = payouts[3];

	}
	public void start(Country cA, Country cB) {
		currGame = 1;
		ctryA = cA;
		ctryB = cB;
		d = 0;
		c = 0;
		aID = cA.ID;
		bID = cB.ID;
		game(cA.strategy.Matrix.get(0), cB.strategy.Matrix.get(0));
	}
	public void game(Bubble aChoice, Bubble bChoice) {
		if (currGame == games) {
			return;
		}
		currGame++;
		if (aChoice.cooperate&&bChoice.cooperate) {
			ctryA.addGains(cPayout[0]);
			ctryB.addGains(cPayout[0]);
			ctryA.addMC(bID);
			ctryB.addMC(aID);
//			ctryA.addOMC();
//			ctryB.addOMC();
//			int[] o = {1, 1};
//			ctryA.write(o);
//			ctryB.write(o);
			c++;
			c++;
			game(aChoice.ifC, bChoice.ifC);
		}else if (aChoice.cooperate&&(!bChoice.cooperate)) {
			ctryA.addGains(cPayout[1]);
			ctryB.addGains(dPayout[0]);
			ctryA.addSC(bID);
			ctryB.addSD(aID);
//			ctryA.addOSD();
//			ctryB.addOSC();
//			int[] oa = {1, 0};
//			int[] ob = {0, 1};
			if(ctryA.industry>0) {
				ctryB.changeIndustry(1);
				ctryA.changeIndustry(-1);	
			}
//			ctryA.write(oa);
//			ctryB.write(ob);
			c++;
			d++;
			game(aChoice.ifD, bChoice.ifC);
		}else if ((!aChoice.cooperate)&&bChoice.cooperate) {
			ctryA.addGains(dPayout[0]);
			ctryB.addGains(cPayout[1]);
			ctryA.addSD(bID);
			ctryB.addSC(aID);
//			ctryA.addOSC();
//			ctryB.addOSD();
//			int[] ob = {1, 0};
//			int[] oa = {0, 1};
			if(ctryB.industry>0) {
				ctryA.changeIndustry(1);
				ctryB.changeIndustry(-1);	
			}
//			ctryA.write(oa);
//			ctryB.write(ob);
			c++;
			d++;
			game(aChoice.ifC, bChoice.ifD);
		}else if ((!aChoice.cooperate)&&(!bChoice.cooperate)) {
			ctryA.addGains(dPayout[1]);
			ctryB.addGains(dPayout[1]);
			ctryA.addMD(bID);
			ctryB.addMD(aID);
//			ctryA.addOMD();
//			ctryB.addOMD();
//			int[] o = {0, 0};
//			ctryA.write(o);
//			ctryB.write(o);
			d++;
			d++;
			game(aChoice.ifD, bChoice.ifD);
		}
	}
	public void game(Bubble aChoice, Bubble bChoice, Bubble aInfo, Bubble bInfo) {
		if (currGame == games) {
			return;
		}
		if (ctryA.GDP <= 0 || ctryB.GDP <= 0) {
			return;
		}
		currGame++;
		if (aChoice.cooperate&&bChoice.cooperate) {
			ctryA.addGains(cPayout[0]);
			ctryB.addGains(cPayout[0]);
			ctryA.addMC(bID);
			ctryB.addMC(aID);
//			int[] o = {1, 1};
//			ctryA.write(o);
//			ctryB.write(o);
			c++;
			c++;
			game(aChoice.ifC, bChoice.ifC);
		}else if (aChoice.cooperate&&(!bChoice.cooperate)) {
			ctryA.addGains(cPayout[1]);
			ctryB.addGains(dPayout[0]);
			ctryA.addSC(bID);
			ctryB.addSD(aID);
//			int[] oa = {1, 0};
//			int[] ob = {0, 1};
			if(ctryA.industry>0) {
				ctryB.changeIndustry(1);
				ctryA.changeIndustry(-1);	
			}
//			ctryA.write(oa);
//			ctryB.write(ob);
			c++;
			d++;
			game(aChoice.ifD, bChoice.ifC);
		}else if ((!aChoice.cooperate)&&bChoice.cooperate) {
			ctryA.addGains(dPayout[0]);
			ctryB.addGains(cPayout[1]);
			ctryA.addSD(bID);
			ctryB.addSC(aID);
//			int[] ob = {1, 0};
//			int[] oa = {0, 1};
			if(ctryB.industry>0) {
				ctryA.changeIndustry(1);
				ctryB.changeIndustry(-1);	
			}
//			ctryA.write(oa);
//			ctryB.write(ob);
			c++;
			d++;
			game(aChoice.ifC, bChoice.ifD);
		}else if ((!aChoice.cooperate)&&(!bChoice.cooperate)) {
			ctryA.addGains(dPayout[1]);
			ctryB.addGains(dPayout[1]);
			ctryA.addMD(bID);
			ctryB.addMD(aID);
//			int[] o = {0, 0};
//			ctryA.write(o);
//			ctryB.write(o);
			d++;
			d++;
			game(aChoice.ifD, bChoice.ifD);
		}
	}
}
