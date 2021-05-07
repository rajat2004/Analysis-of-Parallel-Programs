import syntaxtree.*;
import utils.MHPAnalyzer;
import utils.ParallelExecutionGraph;
import utils.Utils;
import visitor.*;

public class Main {
    public static void main(String[] args) {
        try {
            boolean turn_off_all_debug = false;
            if (turn_off_all_debug) {
                Utils.debug = false;
                SymbolTableGenerator.debug = false;
                PEGConstructor.debug = false;
                MHPQuery.debug = false;
            }

            Node root = new QParJavaParser(System.in).Goal();
            print("Parsed successfully.");

            SymbolTableGenerator st_gen = new SymbolTableGenerator();
            for(int i=0; i<st_gen.n_iterations; i++) {
                print("SymbolTableGen: Iteration " + (i+1));
                root.accept(st_gen, null);
            }

            PEGConstructor peg_constructor = new PEGConstructor(st_gen.st);
            int peg_const_iter = 0;
            while (peg_constructor.next_iteration) {
                print("PEGConstructor: Iteration " + peg_const_iter);
                peg_const_iter++;
                root.accept(peg_constructor, null);
            }

            ParallelExecutionGraph peg = peg_constructor.peg;
            MHPAnalyzer mhp_analyzer = new MHPAnalyzer(peg);
            mhp_analyzer.run();

            print("Answering MHP Queries!");
            MHPQuery mhp_query = new MHPQuery(peg);
            root.accept(mhp_query, null);

        } catch (ParseException e) {
            System.out.println(e.toString());
        }
    }

    private static void print(String s) {
        Utils.print("\n\n\n" + s);
    }
}
