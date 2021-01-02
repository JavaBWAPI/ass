[![Build Status](https://travis-ci.com/Bytekeeper/ass.svg?branch=master)](https://travis-ci.com/Bytekeeper/ass)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/Bytekeeper/ass)](https://ci.appveyor.com/project/Bytekeeper/ass)

# Agent Starcraft Simulator
A hard problem for BW bots is the decision on when to attack and what to attack.
Part of the problem is the question "who will win?"

ASS tries to answer that question by allowing a simulation of possible outcome.

Additionally, various utilities for path-finding or fast location queries are also available. 

## JavaDoc
http://ass.bytekeeper.org/

## References

[Appveyor Build Artifact](https://ci.appveyor.com/project/Bytekeeper/ass/build/artifacts)

[Javadoc](http://ass.bytekeeper.org/)

[JBWAPI](https://github.com/JasperGeurtz/JBWAPI/)

[BWAPI4J](https://github.com/OpenBW/BWAPI4J)

[BWMirror](https://github.com/vjurenka/BWMirror)

###### Credits

* [Fco. Javier Sacido](https://github.com/Jabbo16) for his JFAP port
* [Hannes Bredberg](https://github.com/N00byEdge) for the original FAP  


## Usage
While the simulator is API independent, [JBWAPI](https://github.com/JasperGeurtz/JBWAPI/)
and [BWAPI4J](https://github.com/OpenBW/BWAPI4J) work out of the box. 
[BWMirror](https://github.com/vjurenka/BWMirror) should also
work but is not thoroughly tested. 

### Gradle

Add the maven repo: 

    	allprojects {
    		repositories {
    			...
    			maven { url 'https://jitpack.io' }
    		}
    	}
    	
And the dependency:    	

      dependencies {
        implementation 'com.github.Bytekeeper:ass:1.1'
      }


### Maven

Add the maven repo:
  
    <repositories>
      <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
      </repository>
    </repositories>

And the dependency:

    <dependency>
        <groupId>com.github.Bytekeeper</groupId>
        <artifactId>ass</artifactId>
        <version>1.1</version>
    </dependency>

### JAR file

To get it, either download and build it yourself or grab the
[Appveyor Build](https://ci.appveyor.com/project/Bytekeeper/ass/build/artifacts).

`Agent` is the `Unit` abstraction being used. It can be either created directly, or 
the `BWAPI4JAgentFactory` (resp. `BWMirrorAgentFactory`) can be used to create an `Agent` for an existing `Unit`.
Creating `Agents` by using just a `UnitType` is also possible.

# Simulator
The main class is `Simulator`. You can add `Agents` for player A or player B here.
After doing that, you can simulate a number of frames (default: 96). Next, you
retrieve the result and check if it's to your liking (some of your units survived?).

```java
BWMirrorAgentFactory factory = new BWMirrorAgentFactory(game);
Simulator simulator = new Builder().build(); // You can also customize the simulator

simulator.addAgentA(factory.of(someOfMyUnit));
simulator.addAgentB(factory.of(someEnemy));

simulator.simulate(240); // Simulate 24 seconds

if (simulatior.getAgentsA().isEmpty()) {
// Uh oh
}  else {
// Hurray
}
```

## Features
Simulates:
* Medics
* SCV repair
* Suiciding units (scourge, scarabs, ...)
* Ground attackers
* Air attackers
* Basic movement
* Kiting
* Elevation affecting damage
* Cloaked units
* Splash (Radial, Line and "Bounce" aka Tanks, Lurkers and Mutas)
* Stim, Armor, Weapon, Range and Speed upgrades
* Effects like plague, lockdown, stasis, dark swarm
* Frame skipping to improve simulation performance at cost of precision
* Spider mines

## Limitations
* Elevation is deemed "constant" within the simulation
* Visibility is ignored (visibility is "constant" within the simulation)
* (Most) Spellcasters are doing nothing
* Distance mechanism does not match BW's "boxed" distances
* Instant acceleration 

## Behaviors
Default behaviors:

Medics:
* Find any unit that can be healed this frame (in range and damaged)
* Otherwise find unit in range and in need of healing and move toward it

Suiciders:
* Find any unit that can be attacked this frame and dive in
* Otherwise find targetable unit and close in

Repairers (SCVs):
* Find any unit that can be repaired this frame and repair it
* Find closest unit that can be repaired and move toward it
* If no unit can be repaired, attack

Attackers:
* Find any unit that can be attacked this frame and attack
* Otherwise find viable target unit and close in
* Otherwise flee from any unit that could attack us

You can also use the `RetreatBehavior` to make some or all units run away instead of attacking.

## Evaluator
Another way to estimate outcome of a battle is to use the `Evaluator`. It does not simulate
agents as the `Simulator does`. Instead it uses some heuristics to determine a 
"how well is player A going to be vs B" ranging from [0-1].

The basic idea is:
* Let all agents of A shoot at B and all agents of B shoot at A
* Divide through the combined health to determine how many agents would have died in that round
* Medic heal, health and shield regen are also factored in


# Additional APIs

## Modified DBScan
A DBScan based clustering algorithm.
* Stable clustering: Unless a cluster is split up, units will end up in the same cluster as in previous runs
* Iterative: Instead of assigning all units to clusters at once, do it iteratively. Once done, 
  the clustering restarts and the previous result can be accessed.

## PositionQueries
A utility class to make 2D-position based queries:
* radius queries
* area queries
* nearest queries 

## Jump Path Search
An implementation of the algorithm described here: https://zerowidth.com/2013/05/05/jump-point-search-explained.html

Generally much faster that a standard A* while still being optimal.

## Resource management classes
* GMS class to manage gas, minerals and supply in one value type.
  * Can be used to manage existing resources vs cost of units, tech or upgrades

## Unit and resource locking
Helps in managing resources and units with specific tasks:
* Locks on resources can be used to determine if something can be bought now or even if it can be afforded later
* Locks on units helps for things like building: Find a worker and keep it until its job is done

## Grids
Used for pathing. In addition to JPS, it's also possible to "cast rays" in a grid. This is mostly used
for direct reachability checks (ie. can unit A reach position B without obstacle).

## Behavior Trees
Can be used to control individual units, groups of units of even a complete bot (ie. 
[StyxZ](https://github.com/Bytekeeper/Styx) is controlling basically everything using these).
There are also nodes to simplify locking of resources (ie. units and minerals).
