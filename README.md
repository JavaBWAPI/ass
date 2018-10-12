[![Build Status](https://travis-ci.com/Bytekeeper/ass.svg?branch=master)](https://travis-ci.com/Bytekeeper/ass)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/Bytekeeper/ass)](https://ci.appveyor.com/project/Bytekeeper/ass)

# Agent Starcraft Simulator
A hard problem for BW bots is the decision on when to attack and what to attack.
Part of the problem is the question "who will win?"

ASS tries to answer that question by allowing a simulation of possible outcome.

## Usage
While the simulator is API independent, [BWAPI4J](https://github.com/OpenBW/BWAPI4J)
is working out of the box.

To get it, either download and build it yourself or grab the
[Appveyor Build](https://ci.appveyor.com/project/Bytekeeper/ass/build/artifacts).

`Agent` is a the `Unit` abstraction used. It can be either created directly, or 
the `BWAPI4JAgentFactory` can be used to create an `Agent` for an existing `Unit`.
Creating `Agents` by using just a `UnitType` is also possible.

### Simulator
The main class is `Simulator`. You can add `Agents` for player A or player B here.
After doing that, you can simulate a number of frames (default: 96). Next, you
retrieve the result and check if it's to your liking (some of your units survived?).

### Evaluator
Another way to estimate outcome of a battle is to use the `Evaluator`. It does not simulate
agents as the `Simulator does`. Instead it uses some heuristics to determine a 
"how well is player A going to be vs B" ranging from [0-1].

## Features
Simulates:
* Medics
* Suiciding units (scourge, scarabs, ...)
* Ground attackers
* Air attackers
* Basic movement
* Kiting
* Elevation affecting damage
* Cloaked units

## Limitations
* No collision
* Elevation is deemed "constant" within the simulation
* Visibility is ignored
* Spellcasters are doing nothing
* Distance mechanism does not match BW's "boxed" distances
* Instant movement 

## Behaviors
Medics:
* Find any unit that can be healed this frame (in range and damaged)
* Otherwise find closest unit in need of healing and move toward it

Suiciders:
* Find any unit that can be attacked this frame and dive in
* Otherwise find closest targetable unit and close in

Attackers:
* Find any unit that can be attacked this frame and attack
* Otherwise find closest targetable unit and close in
* Otherwise flee from any unit that could attack us

## Credits
* [Fco. Javier Sacido](https://github.com/Jabbo16) for his JFAP port
* [Hannes Bredberg](https://github.com/N00byEdge) for the original FAP  

