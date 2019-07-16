package lpmaker;


        import java.security.SecureRandom;
        import java.util.*;
        import java.io.*;
        import lpmaker.graphs.*;
        import java.text.DateFormat;
        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.Calendar;

public class TestRunAvg_rdc {
    public static void main(String args[]) throws IOException {
        int numToRs = 4;
        int serversPerToR = 8;
        int osRatio = 4;
        int trafficMode = Integer.parseInt(args[0]);
        int trial = Integer.parseInt(args[1]);
        int para = 0;
        String trafficName = new String();
        RDC static_rdc = new RDC(numToRs,serversPerToR, osRatio);

        ArrayList<TrafficPair> trafficPattern = null;
        if(trafficMode == 0){
            para = numToRs * serversPerToR;
            trafficName="perm";
            trafficPattern = static_rdc.RandomPermutationPairs(para);

        }else if(trafficMode == 11){
            para = serversPerToR;
            trafficName="stride";
            trafficPattern = static_rdc.TrafficGenStride(para);
        }else if(trafficMode == 15){
            //make it rack to rack
            para = serversPerToR;
            trafficName = "m2m";
            trafficPattern = static_rdc.TrafficGenManyToMany(para);

        }else if(trafficMode == 17){
            trafficName = "rackHotspot";
            trafficPattern = static_rdc.TrafficGenRackHotspot(serversPerToR);
        }

        String filename = "TrafficPattern:"+trafficName+"_osRatio:" + osRatio + "_trial:"+trial;
        System.out.println(filename);

        ArrayList<TrafficPair>  convertedTP = convertedTraffic(trafficPattern, trafficName);

        RDC nonBlocking = new RDC(numToRs, serversPerToR, 1);
        static_rdc.PrintGraphforMCFFairCondensedAverage(filename+".static.lp", trafficPattern);
        nonBlocking.PrintGraphforMCFFairCondensedAverage(filename+".nonBlck.lp", trafficPattern);

        static_rdc.PrintGraphforMCFFairCondensedAverage(filename+".rdc.lp", convertedTP);

    }

    public static ArrayList<TrafficPair> convertedTraffic(ArrayList<TrafficPair>original, String trafficName)throws IOException {
        ArrayList<TrafficPair> convertedTP = new ArrayList<>();
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(date);
        String filename = strDate+trafficName + ".tp";
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (TrafficPair pair : original) {
            writer.write(String.valueOf(pair.from));
            writer.write(",");
            writer.write(String.valueOf(pair.to));
            writer.write('\n');
        }
        writer.close();

        String[] cmd = {
                "python",
                "metis.py",
                filename,
                "4"
        };
        Runtime.getRuntime().exec(cmd);

        String convertedFile = filename+".mapped";

        BufferedReader reader = new BufferedReader(new FileReader(convertedFile));

        String currentLine = reader.readLine();
        while(currentLine != null){
           String[] parsed = currentLine.split(",");
           int a = Integer.parseInt(parsed[0]);
           int b = Integer.parseInt(parsed[1]);
           convertedTP.add(new TrafficPair(a, b));
           currentLine = reader.readLine();
        }
        reader.close();
        return convertedTP;
    }
}