package org.apache.mxnetexamples.infer.javapi.objectdetector;

import org.apache.mxnet.infer.javaapi.Predictor;
import org.apache.mxnet.javaapi.*;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DLModelExample {

    final static Logger logger = LoggerFactory.getLogger(DLModelExample.class);

    @Option(name = "--iter", usage = "Number of iterations to run it for")
    private int iter = 10;

    public static void main(String[] args) {

        DLModelExample inst = new DLModelExample();
        CmdLineParser parser = new CmdLineParser(inst);

        try {
            parser.parseArgument(args);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            parser.printUsage(System.err);
            System.exit(1);
        }

        String modelPathPrefix = "mxnet_model";
        List<DataDesc> desc = new ArrayList<DataDesc>();
        List<Context> contextList = new ArrayList<Context>();

        Context ctx = null;


        if (System.getenv().containsKey("SCALA_TEST_ON_GPU") &&
                Integer.valueOf(System.getenv("SCALA_TEST_ON_GPU")) == 1) {
            ctx = Context.gpu();
        } else {
            ctx = Context.cpu();
        }

        contextList.add(ctx);
        desc.add(new DataDesc("/embedding_1_input1", new Shape(new int[] {32, 20}), DType.Float32(), "NT"));

        Predictor predictor = new Predictor(modelPathPrefix,
                desc, contextList, 0);
        NDArray nd = NDArray.ones(ctx, new int[]{32, 20});
        List<NDArray> inputs = new ArrayList<NDArray>();
        inputs.add(nd);


        System.out.println(inst.iter  + " Iter Val");
        long[] times = new long[inst.iter];
        for (int i = 0; i < times.length; i++) {

            Long time = System.currentTimeMillis();
            predictor.predictWithNDArray(inputs);
            time = System.currentTimeMillis() - time;
            times[i] = time;
        }
        printStats(times);

    }

    public static long percentile(int percentile, long[] values) {
        Arrays.sort(values);
        int idx = (int) Math.ceil((values.length - 1) * (percentile / 100.0));
        return values[idx];
    }

    static void printStats(long[] times)  {

        long p50 = percentile(50, times);
        long p99 = percentile(99, times);
        long p90 = percentile(90, times);
        long sum = 0;
        for (long x: times) {
            sum+=x;
        }
        double average = sum / (times.length * 1.0);

        StringBuilder builder = new StringBuilder();
        builder.append("DLMODEL_p99 : " + p99 + "\n");
        builder.append("DLMODEL_p90 : " + p90 + "\n");
        builder.append("DLMODEL_p50 : " + p50 + "\n");
        builder.append("DLMODEL_average : " + average);

        System.out.println(builder.toString());
        logger.info(builder.toString());

    }
}
