# Hex Game with AI Implementation

## Overview
This project implements the board game Hex with an intelligent AI opponent using Monte Carlo Tree Search and minimax algorithms. Hex is a connection strategy game played on a hexagonal grid where players aim to create a continuous path from one side of the board to the other.

## Game Features
- Interactive 7x7 hexagonal board
- Player vs AI gameplay
- Option to play as either Black or White pieces
- Visual representation of the game state
- Bridge detection and strategic AI decision-making

## AI Implementation
The AI player uses a combination of techniques to make intelligent moves:

### Monte Carlo Tree Search (MCTS)
- Evaluates potential moves by simulating game outcomes
- Uses n-completion values to assess board states
- Considers bridge formations and potential blocking moves
- Prioritizes moves that create shorter paths to victory

### Minimax Algorithm
- Implements a recursive minimax algorithm with configurable depth
- Evaluates board states based on path lengths
- Special case handling for opening moves and bridge detection
- Heuristic evaluation based on comparing AI and opponent path lengths

## Components
- **AI.java**: Abstract interface for AI players
- **Board.java**: Board manipulation utilities
- **Bridge.java**: Representation of bridge formations on the Hex board
- **Constants.java**: Game constants (colors, etc.)
- **Location.java**: Coordinates and adjacency calculations
- **Main.java**: Game initialization
- **MCAI.java**: Monte Carlo AI implementation
- **Panel.java**: UI and game logic
- **Utils.java**: Helper functions

## How to Play
1. Run the application using the Main class
2. Choose your preferred color (Black or White)
3. Click on the board to place your pieces
4. The AI will automatically make its move after you play
5. The goal is to form a continuous path from your edge to the opposite edge

## Strategic Elements
- **Bridges**: Special formations that allow indirect connections
- **Path Planning**: The AI constantly evaluates the shortest paths to victory
- **Opening Theory**: Special handling for first and second moves

## Technical Details
- Written in Java with Swing for the UI
- Uses custom drawing routines for the hexagonal game board
- Implements efficient graph traversal algorithms for path finding
- Object-oriented design with clear separation of concerns

## Author
- Thanks to David Pearson for the original code base
