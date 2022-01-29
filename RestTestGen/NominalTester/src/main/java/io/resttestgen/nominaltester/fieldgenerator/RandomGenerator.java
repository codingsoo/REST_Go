package io.resttestgen.nominaltester.fieldgenerator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Random;

public class RandomGenerator {

    static final Logger logger = LogManager.getLogger(RandomGenerator.class);

    protected final Random generator;

    /**
     * Random generator's constructor
     * It uses a specified seed
     * @param seed random generator's seed
     */
    public RandomGenerator(int seed){
        this.generator = new Random(seed);
        logger.info("Random generator seed: " + seed);
    }

    /**
     * RandomGenerator's constructor
     * It uses the current time as a seed
     */
    public RandomGenerator(){
        long rgenseed = System.currentTimeMillis();
        this.generator = new Random(rgenseed);
        logger.info("Random generator seed: " + rgenseed);
    }

    /**
     * Get random integer between min and max
     * @param min minimum value (included)
     * @param max maximum value (excluded)
     */
    public int getRandomInteger(int min, int max){
        if (min == max) return min;
        double randomNumber = getRandomDouble(min, max);
        return (int)randomNumber;
    }

    /**
     * Get random Double between min and max
     * @param min minimum value (included)
     * @param max maximum value (excluded)
     */
    public double getRandomDouble(double min, double max){
        if (min == max) return min;
        return min + this.generator.nextDouble() * (max - min);
    }

    /**
     * Get random long between min and max
     * @param min minimum value (included)
     * @param max maximum value (excluded)
     */
    public long getRandomLong(long min, long max){
        if (min == max) return min;
        return min + this.generator.nextLong() * (max - min);
    }

    /**
     * Get a random element inside a collection
     *
     * @param collection source collection
     * @param <T> Type of element inside the collection
     * @return random element inside the collection, null if empty
     */
    public <T> T getRandomElementFromCollection(Collection<T> collection) {
        int max = collection.size();
        int min = 0;
        int randomIndex = getRandomInteger(min, max);
        for(T t: collection) if (--randomIndex < 0) return t;
        return null;
    }

    /**
     * Generate random String given constraints
     * @param minLength minimum length
     * @param maxLength maximum length
     * @param useLetters should include letters
     * @param useNumbers should include numbers
     * @return Random String
     */
    public String getRandomString(int minLength, int maxLength, boolean useLetters, boolean useNumbers){
        int length = getRandomInteger(minLength, maxLength);
        if (!useLetters && !useNumbers){
            // returns spaces
            StringBuilder spaces = new StringBuilder();
            for (int i = 0; i < length; i++) {
                spaces.append(" ");
            }
            return spaces.toString();
        }
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }
}
