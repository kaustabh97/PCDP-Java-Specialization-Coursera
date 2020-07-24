package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import java.util.ArrayList;
import java.util.List;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 *
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determine the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     *
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */

    @Override
    public int countPrimes(final int limit) {

        final SieveActorActor currSieve = new SieveActorActor();

        finish(() -> { for (int i = 3; i <= limit; i += 2) {
                currSieve.send(i);
            }
            currSieve.send(0);
        });


        int totalNumPrimes = 1;
        SieveActorActor countSieve = currSieve;
        while (countSieve != null){
            totalNumPrimes += countSieve.numLocalPrimes;
            countSieve = countSieve.nextSieve;
        }

        return totalNumPrimes;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        /**
         * Process a single message sent to this actor.
         *
         * TODO complete this method.
         *
         * @param msg Received message
         */
        static final int MAX_LOCAL_PRIMES = 1000;

        SieveActorActor nextSieve = null;

        int numLocalPrimes = 0;
        List<Integer> localPrimes = new ArrayList<>();

        @Override
        public void process(final Object msg) {
            int candidate = (int) msg;

            if(candidate <= 0){
                if(nextSieve!=null){
                    nextSieve.send(msg);
                }
                return;
            }

            if(!checkPrime(candidate)){
                return;
            }

            if(numLocalPrimes < MAX_LOCAL_PRIMES){
                localPrimes.add(candidate);
                numLocalPrimes++;
                return;
            }

            if(nextSieve == null){
                nextSieve = new SieveActorActor();
            }
            nextSieve.send(msg);
        }

        private boolean checkPrime(final int candidate) {

            for (int i = 0; i < numLocalPrimes; i++) {
                if (candidate % localPrimes.get(i) == 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
