package benchmark;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.khkhlv.balltree.BallTree;
import ru.khkhlv.balltree.Knn;
import ru.khkhlv.balltree.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = {"-Xms2G", "-Xmx2G"})
@Warmup(iterations = 2, time = 100, timeUnit = TimeUnit.MILLISECONDS)
public class BallTreeBenchmark {

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(BallTreeBenchmark.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
    private List<RealVector> vectors;
    private RealVector target;
    private BallTree ballTree;
    private Node root;
    private List<RealVector> kActualNeighbours;
    private List<RealVector> foundNeighbours;
    private static final Logger logger = LoggerFactory.getLogger(BallTreeBenchmark.class);

    @Param({"1", "5", "10", "100", "1000"})
    private int leafSize;

    @Param({"100", "1000"})
    private int k;

    @Setup
    public void setUp() {
        String fileName = "src/test/resources/dataset.txt";
        List<List<Double>> twoDList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split("\\s+");
                List<Double> row = new ArrayList<>();
                for (String value : values) {
                    row.add(Double.parseDouble(value));
                }
                twoDList.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.vectors = twoDList.stream().map(doubleList -> new ArrayRealVector(doubleList.stream().mapToDouble(Double::doubleValue).toArray())).collect(Collectors.toList());
        target = vectors.get(ThreadLocalRandom.current().nextInt(0, vectors.size()));
        ballTree = new BallTree(root, leafSize);
        root = ballTree.buildTree(vectors);
        foundNeighbours = new Knn().knn(this.vectors,k, target);

    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    public void testBallTree() {
        kActualNeighbours = ballTree.searchTree(root, target, k);
    }

    private Boolean neighbourIsPresent(double[] array) {
        return kActualNeighbours.stream().map(RealVector::toArray).anyMatch(arr -> Arrays.equals(array, arr));
    }

    @TearDown(Level.Trial)
    public void estimateResult() {
        logger.info("Search {} neighbours in tree", k);
        if (foundNeighbours != null) {
            double n = foundNeighbours.stream()
                    .map(RealVector::toArray)
                    .filter(this::neighbourIsPresent)
                    .count();
            logger.info("Found {} out of {} neighbours, which is {} precision", n, k, n / k);
        }
    }
}