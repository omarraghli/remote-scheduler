# Remote Schedule Generator

## The Problem

**The Chaos of Manual Scheduling:**

Every week, our team faces the same chaotic situation when the remote work schedule board goes live:

**What Happens:**
1. The Excel file is shared on Teams/SharePoint
2. Everyone rushes to fill in their preferred remote days
3. Multiple people edit the file simultaneously
4. **Concurrency nightmare begins:**
    - People accidentally overwrite each other's names
    - Slots get double-booked
    - Names disappear as someone saves an older version
    - Conflicts arise over who claimed a slot first
5. Back-and-forth messages trying to resolve conflicts
6. The schedule has to be manually fixed multiple times

**The Constraints That Make It Worse:**
1. **Daily Capacity:** Only 4 slots available (5 on Wednesday)
2. **Individual Quota:** Each person needs exactly 3 remote days
3. **Consecutive Days Rule:** No one can work remotely more than 2 days in a row
4. **Holidays:** Some days may be holidays with zero remote slots
5. **Vacation Returns:** People returning from vacation can't work remotely on their return day

**Real Impact:**
- Wasted time every Friday dealing with scheduling conflicts
- Frustration from accidentally deleted names
- Unfair advantages for those who fill the sheet first
- Version control issues with the Excel file
- Team stress and unnecessary friction

**The Solution:**

Instead of a chaotic free-for-all, this algorithm automatically generates a fair schedule that:
- Respects all constraints automatically
- Eliminates concurrency issues (no more simultaneous edits)
- Ensures fairness through randomization
- Takes seconds instead of hours
- Removes human error and conflicts

## Solution

This Java application uses a **constraint-based backtracking algorithm** to automatically generate valid remote work schedules that satisfy all requirements.

## How It Works

### Core Algorithm

The solution uses **randomized backtracking search** with the following approach:

1. **Constraint Definition**
    - Each person gets exactly 3 remote days per week
    - Maximum 2 consecutive remote days per person
    - Respect daily slot limits (4 or 5 depending on the day)
    - Handle holidays (zero slots)
    - Respect vacation return dates

2. **Search Strategy**
    - Generate all valid combinations of people for each day's available slots
    - Use backtracking to explore possible schedules
    - Randomize selection order to ensure fairness
    - Stop at first valid solution found

3. **Bitmask Representation**
    - Each day's assignment is stored as a bitmask integer
    - Bit position represents a person (bit set = working remotely)
    - Enables efficient combination generation and validation

### Key Components

#### Configuration Constants
```java
BASE_SLOTS = 4           // Default slots per day
REMOTES_PER_PERSON = 3   // Remote days per person per week
EXTRA_DAY = 2            // Wednesday (index 2) has 5 slots
HOLIDAYS                 // Set of days to skip
```

#### Constraint Validation
- **Consecutive Days Check:** Tracks consecutive remote days per person
- **Quota Check:** Ensures no one exceeds 3 remote days
- **Vacation Constraints:** Blocks assignment on return days
- **Holiday Handling:** Skips processing for configured holidays

#### Output
- Console display of the generated schedule
- Excel export (`.xlsx` format) with formatted columns
- Clear indication of holidays and special slot days

## Usage

### Basic Configuration

1. **Define Team Members**
```java
static final String[] PEOPLE_ORIGINAL = {"Oussama", "Outman", "Ayoub", "Omar", "Yamin", "Sara", "Hamza"};
```

2. **Set Holidays**
```java
static final Set<String> HOLIDAYS = Set.of("Lundi"); // Monday is a holiday
```

3. **Configure Vacation Returns** (optional)
```java
Map<String, String> vacationReturns = Map.of(
    "Oussama", "Lundi",    // Oussama returns on Monday (can't work remote that day)
    "Sara", "Jeudi"        // Sara returns on Thursday
);
```

### Running the Application

```bash
# Compile (requires Apache POI in classpath)
javac -cp ".:poi-5.x.x.jar:poi-ooxml-5.x.x.jar" Main.java

# Run
java -cp ".:poi-5.x.x.jar:poi-ooxml-5.x.x.jar:..." cires.Main
```

### Output

**Console Output:**
```
Initializing remote schedule generator...
Configuration: Mercredi has 5 available slots this week
Holidays configured: Lundi

Generated Weekly Schedule:
 Lundi: Holiday - No remote work
 Mardi: Oussama, Ayoub, Omar, Yamin
 Mercredi: Outman, Sara, Hamza, Omar, Yamin
 Jeudi: Oussama, Ayoub, Outman, Sara
 Vendredi: Oussama, Ayoub, Sara, Hamza

Schedule successfully exported to: remote_schedule.xlsx
```

**Excel Output:**
- Columns represent days of the week
- Rows contain names of people working remotely
- Headers indicate holidays and special slot days

## Dependencies

- **Apache POI 5.x** - Excel file generation
    - `poi-5.x.x.jar`
    - `poi-ooxml-5.x.x.jar`
    - `poi-ooxml-schemas-5.x.x.jar`
    - `commons-collections4-4.x.jar`
    - `xmlbeans-5.x.x.jar`

## Customization

### Adjust Remote Days Per Person
```java
static final int REMOTES_PER_PERSON = 3; // Change to desired number
```

### Modify Slot Distribution
```java
static final int BASE_SLOTS = 4;         // Default slots
static final int EXTRA_DAY = 2;          // Day index for extra slot
slotsPerDay[EXTRA_DAY] = BASE_SLOTS + 1; // Modify slot count
```

### Add More Days
```java
static final String[] DAYS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
```

## Algorithm Complexity

- **Time Complexity:** O(C^n) where C is combinations per day, n is number of days
- **Space Complexity:** O(n * m) where n is days, m is people
- **Optimization:** Randomization with early exit significantly reduces average-case runtime

## Limitations

- May not find a solution if constraints are too restrictive
- Does not optimize for specific preferences or priorities
- First valid solution is returned (not necessarily "best")

## Contributing

Pull requests are welcome! Areas for improvement:
- Add weighted preferences for specific days
- Implement solution quality scoring
- Support for partial remote days
- Web interface for easier configuration
- Multi-week planning with rotation fairness

## License

[Add your license here]

## Author

Created to solve weekly remote schedule assignment challenges for the mobility team.