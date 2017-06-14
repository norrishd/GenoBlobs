# GenoBlobs
David Norrish, 2016-2017

GenoBlobs is a simple evolutionary simulator. It consists of coloured balls
that bounce around an environment and periodically divide, spawning offspring with
slightly mutated attributes.

Using only a small set of rules and features, it seeks to recapitulate a range of evolutionary processes.

## Evolutionary/biological processes implemented so far:
1. Blobs bounce around the environment (like proto-bacteria in a primordial sea)
2. Blobs gain "power" over time (e.g. by photosynthesis) and spawn children when they have accrued sufficient power
3. Children have slightly mutated attributes compared to their parent
4. Blobs are named using a convention that tracks their phylogeny
5. Blobs spawn small and grow to full size by "adulthood", which is halfway to their spawn threshold
6. Defence and attack reach max values at adulthood
7. Blobs become more opaque as they near spawning time (for user benefit, but could also represent cytoplasm volume)
8. MutationRate determines child attributes, including new mutationRate
9. Simulation rules guide attribute evolution
10. Blobs recognise and tolerate family members (siblings, parents, aunts, uncles, grandparents and 1st cousins)
11. Altruism system whereby similar Blobs may gift each other some power
12. Avoidance system whereby somewhat dissimilar Blobs will bounce apart
13. Attack system whereby sufficiently dissimilar Blobs will fight

## Technical mechanics:
1. Movement:
    - Bouncing off walls randomly slightly alters velocity in perpendicular trajectory to prevent Blobs becoming trapped in 1D path
    - Hitting a wall increments velocity in that plane by 1 to prevent Blobs coming to rest touching a wall and then
        accelerating in the perpendicular direction because of the previous rule
    - Blobs that spawn overhanging viewer won't become trapped but will naturally bounce back in
2. Spawning
    - Occurs when a Blob's power > its spawnThreshold and not overlapping any other Blobs
    - Power grows at the rate of powerGrowthRate - (radius*favSpeed/10)
    - Child spawns somewhere ~within its parent's radius
    - Child begins at at ~2/3 maximal radius and adult-level opacity (0.8)
    - Reach adult radius when power = spawnThreshold/2
    - When at 80% to spawning power, opacity linearly increases up to 1.0
3. Mutation
    - MutationRate + a random number generator determine every stat of each child
    - MutationRate also mutates the mutationRate
    - Maximal mutationRate multiplied by a rare nextGaussian of 2 should give a 50% change in trait over realistic spectrum
4. Collisions
    - 2 phase system: first checks AABB overlap, then checks for actual Blob collision
    - Then 3 checks for behaviour:
        1. Help? Determined by similarity and altruism; results in friendly or undecided
            - Children are generally ~190000-195000 (can be as low as 183000 with high mutation)
            - Close ancestors are ~ 180000-190000
        2. Ignore? Determined by common ancestry (i.e. name); results in neutral or undecided
        3. Fight/avoid? Determined by similarity and xenophobia; results in wary or hostile
            - Similar looking strangers are ~170000-185000
            - Dissimilar looking strangers are ~<165000
        - Game theory type tree:
            - If either Blob friendly and the other friendly or neutral, friendly(s) help(s)
            - If both neutral, pass through
            - If either is wary and neither is hostile, harmless bounce
            - If either is hostile, attack. If one hostile and the other is friendly or neutral, hostile is attacker;
              else randomise
    - Parameters for bouncing have to be divided by 100 to get realistic effect. No idea why.
5. Misc
    - Was getting exception in thread "JavaFX Application Thread" java.util.ConcurrentModificationException error
        - Fixed by removing ArrayList update in timeline loop, and instead only storing Blobs in Viewer group


## Yet to implement:
- Background environment
- Allow cells to die by old age
- Add predator movement pattern, of not bouncing but rather greatly slowing before resuming in same trajectory
- Figure out fair way to kill certain cells when becomes too crowded
- Incorporate elements besides colour into relatedness (cultural similarity)
- Add a "special attributes" array to each Blob, where it can evolve things like
    - carnivory
    - sexual reproduction
    - damage repair
    - green beards
    - shoot off young to nearby location
- Make child stroke start growing from 0?
- Random events that kill some Blobs
- Number of ancestors back to recognise as kin evolvable
- System so that Blobs can be neutral to quite different but obviously harmless other Blobs
- Blobs can help other Blobs multiple time, but only after un-overlapping
- Splatter animation upon death
- Sprites for altruism/combat
- Figure out a better trade-off between growth rate, movement, size, attack, defence and special abilities
- Standardise evolution of traits to that mutationRate of 1 * nextGaussian of 1 gives 50% change in trait
- Evolvability of how many generations back to help kin
- Barriers (reproductive isolation)
- Initialiser screen to set parameters for game
- Mouse click on Blob to display information about it
- Write evolutionary trends over time to file
- Broad phase collision detection algorithm to reduce time complexity below O(n^2)
- Colour determined by attributes?
- Different movement patterns
- Clicking on a blob displays its stats in the panel
- Messages that indicate that milestones have been passed:
    - The first bounce away
    - The first attack
    - First killing
    - First altruistic exchange
- Blob names also build correctly exceeding 25 generations
- Sexual reproduction
- GreenBeards
- Add Tiktaalik music
    - Quiet version prior to first split


## Rules
- Damage:
    - Starts at 0
    - Sustained by collisions with hostile Blobs
    - Heals slowly over time
    - Blob can endure damage up to [some constant] * its radius
        - Therefore small Blobs will die more easily than large ones

- Mutation rate should be general and apply to all traits, including itself
- Spawning costs at most 50% of a Blob’s power to spawn (i.e. max spawnRatio is 0.5)
- There shall be some mimimum spawnThreshold
- There is a net loss of power when spawning (50% of transferred energy?)

- There is a trade-off between growth rate and Euclidian movement speed (sqrt(x^2 + y^2)) at high levels
- It is more energetically expensive to move the larger one's size
- It is more energetically to move quickly

Radius caps spawnThreshold (i.e. smaller Blobs will spawn more quickly)

## Battles
There is a trade-off between attack and defence at high levels – somehow create a bi-modal distribution to encourage specialising in one or the other
In a battle, both Blobs deal each other their attack damage, and receive other’s damage divided by their own defence
Bounce is determined by xenophobia and similarity
Xenophobia determines how dissimilar another Blob has to be to be considered “other”
Similarity is determined by radius and Euclidian distance of colour
Additionally, battle can happen. Determined by battle confidence
Either Blob may initiate battle
Battle confidence is determined by assessing own attack and other blob’s defence
Either have a constant threshold or add an evolvable “aggression” trait
If a Blob kills another, get to steal some power, determined by other Blob’s radius
This can only start evolving after a lineage learns to fight

## All evolutionary processes (ultimately to be) encapsulated
1. Random mutation of attributes
2. Non-random survival, resulting in natural selection
3. Niche specialisation
    - sedentary versus motile
    - autotrophy versus heterotrophy
4. Kin selection
5. Green beards
6. Success of sexual selection over asexual propagation
(7. Intra-species competition being greater than inter-species?)
(8. Different life cycle stages? [e.g. more highly motile adult phase])
