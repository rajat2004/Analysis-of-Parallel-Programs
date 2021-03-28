import syntaxtree.*;
import utils.*;
import visitor.*;

public class Main {
   public static void main(String [] args) {
      try {
		Node root = new QTACoJavaParser(System.in).Goal();

	 	Utils.print("Parsed successfully.");

	 	SymbolTableGenerator stg = new SymbolTableGenerator();
	 	root.accept(stg, null);

	 	Utils.print("SymbolTableGen completed");

	 	AliasAnalyzer aa = new AliasAnalyzer(stg.st);
	 	int iteration = 0;

	 	while(aa.maps_updated) {
            Utils.print("Iteration: " + iteration);
            iteration++;
            root.accept(aa, null);
        }

	 	Utils.print("Map updates completed!");
	 	aa.answer_alias_queries = true;

	 	// Once more unto the breach
        root.accept(aa, null);

		 // GJDepthFirst v = new GJDepthFirst();
         // root.accept(v,null); // Your assignment part is invoked here.
		 // System.out.println("Number of store statements = " + v.cnt);

	// 1. Write code in GJDepthFirst (copy it to CallGraphCreator.java) to create the call-graph.
		// CallGraphCreator v1 = new CallGraphCreator(..);
		// root.accept (v1...)

	// 2. Write code in GJDepthFirst (copy it to AliasAnalyzer.java) to do alias analysis.
		// AliasAnalyzer v2 = new AliasAnalyzer(..);
		// root.accept(v2...)

	 // Feel free to copy/extend GJDepthFirst to newer classes and
	 // instantiate those visitor classes, and "visit" the syntax-tree
	 // by calling root.accept(..)
         
      }
      catch (ParseException e) {
         System.out.println(e.toString());
      }
   }
} 
