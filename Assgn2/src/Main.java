import syntaxtree.*;
import utils.*;
import visitor.*;

public class Main {
   public static void main(String [] args) {
      try {
        Node root = new QTACoJavaParser(System.in).Goal();

        boolean turn_off_all_debug = true;
        if (turn_off_all_debug) {
            Utils.debug = false;
            AliasAnalyzer.debug = false;
            SymbolTableGenerator.debug = false;
        }

        Utils.print("Parsed successfully.");

        SymbolTableGenerator stg = new SymbolTableGenerator();
        root.accept(stg, null);

        Utils.print("SymbolTableGen completed");

        AliasAnalyzer aa = new AliasAnalyzer(stg.st);
        int iteration = 0;

        while(aa.maps_updated) {
            Utils.print("\nIteration: " + iteration);
            iteration++;
            root.accept(aa, null);
        }

        Utils.print("\n\nMap updates completed!");
        aa.answer_alias_queries = true;

        // Once more unto the breach
        root.accept(aa, null);
         
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 
