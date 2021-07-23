import java.io.*;
import java.util.*;

@SuppressWarnings("unchecked")
public class vmsim{
    static int instructionCounter1 = 0;
    static int instructionCounter2 = 0;
    public static void main(String[] args) throws IOException{

      //Because I am too lazy to scroll back to the top of the page, I kinda have 2 places for declarations. this is one
        String stringForSplit = "";
        String traceFile = "";
        String[] arrForParse;
        String alg = "";

        //used for inputs
        int numFramesTot = -1;
        int pageSize = -1;
        int inputPageSize = -1;
        //where each process frames will be counted
        int numFrames1;
        int numFrames2;



        int instructsToDo1 = 0;
        int instructsToDo2 = 0;

        ArrayList<String> fileMirror1 = new ArrayList<String>();
        ArrayList<String> fileMirror2 = new ArrayList<String>();

        ArrayList<String> instructionArr1 = new ArrayList<String>();
        ArrayList<String> instructionArr2 = new ArrayList<String>();

        Hashtable<String, LinkedList<Integer>> addressHash1 = new Hashtable<String, LinkedList<Integer>>();
        Hashtable<String, LinkedList<Integer>> addressHash2 = new Hashtable<String, LinkedList<Integer>>();



        //./vmsim -a <opt|lru> –n <numframes> -p <pagesize in KB> -s <memory split> <tracefile>
        if(args.length == 9){
            if(args[0].equals("-a")){alg = (args[1]);}
            if(args[2].equals("-n")){numFramesTot = Integer.parseInt(args[3]);}
            if(args[4].equals("-p")){inputPageSize = Integer.parseInt(args[5]);}
            if(args[6].equals("-s")){stringForSplit = args[7];}
            traceFile = args[8];
        } else{ //eror
            System.out.println("Input Fail! Wrong Number or Format Args!");
            return;
        }

        arrForParse = stringForSplit.split(":");
        numFrames1 = Integer.parseInt(arrForParse[0]);
        numFrames2 = Integer.parseInt(arrForParse[1]);

        if((numFramesTot % (numFrames1 + numFrames2)) != 0){
            System.out.println("The memory split you entered MUST total a number cleanly divisible by the number of frames. Try Again");
            return;
        }

        while((numFrames1 + numFrames2) < numFramesTot){
          //I'm the king of efficiency
            numFrames1 = numFrames1 + Integer.parseInt(arrForParse[0]);
            numFrames2 = numFrames2 + Integer.parseInt(arrForParse[1]);
        }

        pageSize = inputPageSize * 1024; //This takes kb to just b I believe

        pageSize = (int)(Math.log(pageSize) / Math.log(2)); //Not going to lie this is a stack overflow find but we can only log10, so I do actually understand why and how this works


        File f = new File(traceFile);

        try{
            new Scanner(f);
        } catch(FileNotFoundException e){
            System.out.println("File doesn't exist!");
            return;
        }
        BufferedReader br = new BufferedReader(new FileReader(traceFile));


        while(br.ready()){

            String instruction = br.readLine();
            String address = instruction.substring(2,12);

            //String pageNum = address;

            address=address.trim();
            //System.out.println(address);
            long longPageNum;
            longPageNum = Long.decode(address);

            longPageNum = longPageNum >> pageSize;

            address = String.valueOf(longPageNum);








            if(instruction.substring(13).equals("0")){


                instructionArr1.add(instruction.substring(0,1));

                if(addressHash1.containsKey(address)){
                    LinkedList<Integer> instructList1 = addressHash1.get(address);
                    instructList1.add(instructsToDo1);
                }else{
                    LinkedList<Integer> instructList1 = new LinkedList<Integer>();
                    instructList1.add(instructsToDo1);
                    addressHash1.put(address,instructList1);
                }
                fileMirror1.add(address);
                instructsToDo1 +=1;
            }
            else if(instruction.substring(13).equals("1")){


                instructionArr2.add(instruction.substring(0,1));

                if(addressHash2.containsKey(address)){
                    LinkedList<Integer> instructList2 = addressHash2.get(address);
                    instructList2.add(instructsToDo2);
                }else{
                    LinkedList<Integer> instructList2 = new LinkedList<Integer>();
                    instructList2.add(instructsToDo2);
                    addressHash2.put(address,instructList2);
                }
                fileMirror2.add(address);
                instructsToDo2 +=1;
            }
            else{
                System.out.println("what");
            }

        }
        br.close();
        //System.out.println("Successfully loaded table: " + f);
        int[] run1 = new int[3];
        int[] run2 = new int[3];

        if(alg.equals("opt")){
            run1 = opt(numFrames1, instructsToDo1, fileMirror1, instructionArr1, addressHash1);
            run2 = opt(numFrames2, instructsToDo2, fileMirror2, instructionArr2, addressHash2);

        }
        else if (alg.equals("lru")){
            run1 = lru(numFrames1, instructsToDo1, fileMirror1, instructionArr1, addressHash1);
            run2 = lru(numFrames2, instructsToDo2, fileMirror2, instructionArr2, addressHash2);

        }


        //printVals(run1, alg, numFrames1, inputPageSize);
        //printVals(run2, alg, numFrames2, inputPageSize);


        run1[0] = run2[0] + run1[0];
        run1[1] = run2[1] + run1[1];
        run1[2] = run2[2] + run1[2];
        printVals(run1, alg, numFrames1+numFrames2, inputPageSize);



        /*
        summaryPrint(run1, alg, numFrames1, inputPageSize);
        summaryPrint(run2, alg, numFrames2, inputPageSize);


        run1[0] = run2[0] + run1[0];
        run1[1] = run2[1] + run1[1];
        run1[2] = run2[2] + run1[2];
        summaryPrint(run1, alg, numFrames1+numFrames2, inputPageSize);

         */
    }

    private static void printVals(int[] run, String alg, int numFrames, int pageSize) {

        System.out.println("Algorithm: " + (alg).toUpperCase(Locale.ROOT));
        System.out.println("Number of frames: " + numFrames);
        System.out.println("Page size: " + pageSize + " KB");
        System.out.println("Total memory accesses: " + run[2]);
        System.out.println("Total page faults: " + run[0]);
        System.out.println("Total writes to disk: " + run[1]);
        System.out.println("");


    }

    private static void summaryPrint(int[] run, String alg, int numFrames, int pageSize) {

        //System.out.println("Algorithm: " + (alg).toUpperCase(Locale.ROOT));
        System.out.println("FN: " + numFrames);
        //System.out.println("Page size: " + pageSize + " KB");
        System.out.println("MA: " + run[2]);
        System.out.println("PF: " + run[0]);
        System.out.println("DW: " + run[1]);
        System.out.println("");


    }


    @SuppressWarnings("DuplicatedCode")
    public static int[] opt(int frames, int totalInstructions, ArrayList<String> fileMirror, ArrayList<String> instructionArr, Hashtable<String, LinkedList<Integer>> addressHash){
        //System.out.println("Doing Opt");

        int totalPageFaults = 0;
        int totalWritesToDisk = 0;
        int totalMemoryAccesses = 0;


        Page[] table = new Page[frames];
        for(int i = 0; i < totalInstructions; i++){
            //System.out.println(i);
            String s = fileMirror.get(i);
            Page p = new Page();
            p.setFrame(s);
            boolean found = false;
            if (instructionArr.get(i).equals("s")){
                p.setDirty(true);
            }
            for (int j = 0; j < table.length && !found; j++){
                if (table[j] == null){
                    table[j] = p;
                    totalPageFaults++;
                    found = true;
                }
                else if (table[j].getFrame().equals(p.getFrame())){
                    if (table[j].getDirty()){
                        p.setDirty(true);
                    }
                    table[j] = p;
                    found = true;
                }
            }
            if (!found){
                totalPageFaults++;
                int index = 0;
                int highestValue = -1;
                boolean[] LRUArray = new boolean[table.length];
                boolean subFound = false; // bool yes/no if addresses do not appear in the future
                Arrays.fill(LRUArray, false);
                for (int j = 0; j < table.length; j++){
                    String address = table[j].getFrame();
                    if (addressHash.containsKey(address)){
                        LinkedList<Integer> l = addressHash.get(address);
                        if (!l.isEmpty() && l.getFirst() > highestValue){
                            index = j;
                            highestValue = (int) l.getFirst();
                        }
                        else if (l.isEmpty()){
                            LRUArray[j] = true;
                            subFound = true;
                        }
                    }
                }
                if (subFound){
                    int lowestValue = Integer.MAX_VALUE;
                    for (int j = 0; j < LRUArray.length; j++){
                        if (LRUArray[j] && table[j].age < lowestValue){
                            lowestValue = table[j].age;
                            index = j;
                        }
                    }
                }
                if (table[index].getDirty()){
                    totalWritesToDisk++;
                }
                table[index] = p;
            }
            totalMemoryAccesses++;
            //LinkedList<Integer> l = addressHash.get(Long.toHexString(s));
            LinkedList<Integer> l = addressHash.get(s);
            ///System.out.println(l.getFirst());
            l.removeFirst();

            for (Page page : table) {
                if (page != null) {
                    page.age++;
                }
            }
        }
        int[] retVals = new int[3];
        retVals[0] = totalPageFaults;
        retVals[1] = totalWritesToDisk;
        retVals[2] = totalMemoryAccesses;

        return retVals;
    }

    @SuppressWarnings("DuplicatedCode")
    public static int[] lru(int frames, int totalInstructions, ArrayList<String> fileMirror, ArrayList<String> instructionArr, Hashtable<String, LinkedList<Integer>> addressHash){
        //System.out.println("Doing lru");

        int totalPageFaults = 0;
        int totalWritesToDisk = 0;
        int totalMemoryAccesses = 0;


        Page[] table = new Page[frames];
        for(int i = 0; i < totalInstructions; i++){
            String s = fileMirror.get(i);
            Page p = new Page();
            p.setFrame(s);
            boolean found = false;
            if (instructionArr.get(i).equals("s")){
                p.setDirty(true);
            }
            for (int j = 0; j < table.length && !found; j++){
                if (table[j] == null){
                    table[j] = p;
                    totalPageFaults++;
                    found = true;
                }
                else if (table[j].getFrame().equals(p.getFrame())){
                    if (table[j].getDirty()){
                        p.setDirty(true);
                    }
                    table[j] = p;
                    found = true;
                }
            }
            // Eviction starts here
            if (!found){
                totalPageFaults++;
                int index = 0;
                int highestValue = -1;
                for (int j = 0; j < table.length; j++){
                    if (table[j].age > highestValue){
                        highestValue = table[j].age;
                        index = j;
                    }
                }
                if (table[index].getDirty()){
                    totalWritesToDisk++;
                }
                table[index] = p;
            }
            // Eviction stops here
            totalMemoryAccesses++;

            for (Page page : table) {
                if (page != null) {
                    page.age++;
                }
            }
        }


        int[] retVals = new int[3];
        retVals[0] = totalPageFaults;
        retVals[1] = totalWritesToDisk;
        retVals[2] = totalMemoryAccesses;

        return retVals;

    }

    static class Page{
        public int age;
        private String frame;
        private boolean dirty;


        public Page() {
            this.age = 0;
            this.dirty = false;
        }


        public boolean getDirty(){
            return dirty;
        }

        public String getFrame(){
            return frame;
        }

        public void setDirty(boolean val){
            dirty = val;
        }

        public void setFrame(String val){
            frame = val;
        }
    }



}
