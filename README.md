# grupp8-fangdjupet

**Fångdjupet** är ett textbaserat rollspel i konsolen, byggt i Java av tre personer i kursen
Javaprogrammering (grupp 8). Hjälten tar sig igenom skolan rum för rum – Skoldörren, Kafferummet,
Korridoren och städskrubben och till sist Lektionssalen – och slåss mot fiender som JavaFläcken,
ett troll och en trollkarl, StädHäxan och slutbossen Kapten Martin. Spelaren väljer attack varje
runda, plockar upp vapen och får hjälp av kaffe, kanelbullar och Frida. Grafiken är ren ASCII.

Projektet handlar mest om *hur* det är byggt: tre paket med en ägare var, tre designmönster
(Strategy, Factory och Observer), JUnit-tester och ett Git-arbetsflöde med feature-grenar,
pull requests och granskning.

**Köra spelet:** kör `Main` i IntelliJ (gröna play-knappen), eller i terminalen:
```
mvn -q compile
java -cp target/classes se.fangdjupet.Main
```
Kräver JDK 27 (se *Kom igång* nedan). Testerna körs med `mvn test`.

## Kom igång

### 1. Installera
- **JDK 27 eller nyare** (krävs av `pom.xml`). Enklast via IntelliJ: *File → Project Structure → SDK → Download JDK*.
- **Maven** behövs bara om du vill köra `mvn` i terminalen – IntelliJ har Maven inbyggt.

### 2. Klona och öppna
- IntelliJ: *File → New → Project from Version Control* → klistra in `https://github.com/ithsjava26/grupp8-fangdjupet` → *Clone*.
- IntelliJ känner igen `pom.xml` och laddar projektet som Maven-projekt. Klicka *Load Maven project* om den frågar.

### 3. Kontrollera att allt fungerar
- Kör `Main` (gröna play-knappen) – den ska skriva ut startbilden "Fangdjupet" i ASCII.
- Eller i terminalen:
  ```
  mvn test
  ```
  Ska ge `BUILD SUCCESS`.

## Arbetsflöde

Ingen pushar direkt till `main`. Allt går via egna brancher och pull requests.

```
git checkout main
git pull
git checkout -b feature/<vad-du-gör>
# koda, committa
git push -u origin feature/<vad-du-gör>
```

### Skapa en pull request

När koden är klar på din branch:

1. **Kontrollera att allt fungerar:** `mvn test` ska ge `BUILD SUCCESS`.
2. **Committa och pusha:**
   ```
   git add .
   git commit -m "Kort beskrivning av vad du gjort"
   git push -u origin feature/<vad-du-gör>
   ```
3. **Öppna pull requesten på GitHub:**
   - Gå till https://github.com/ithsjava26/grupp8-fangdjupet. Efter en push visas en gul ruta med **Compare & pull request** – klicka på den.
   - Syns ingen ruta: fliken **Pull requests** → **New pull request** → välj `base: main` och `compare: feature/<vad-du-gör>`.
   - Skriv en titel och kort beskrivning: vad du gjort och hur man testar det.
   - Välj en gruppmedlem under **Reviewers**.
   - **Assigna pull requesten till en person** under **Assignees** (högerspalten): den som ansvarar för PR:en, oftast du själv – klicka **assign yourself**. Utan assignee syns det inte vem som äger den i listan under [Pull requests](https://github.com/ithsjava26/grupp8-fangdjupet/pulls).
   - Klicka **Create pull request**.

   Har du redan skapat PR:en utan assignee: öppna den under [Pull requests](https://github.com/ithsjava26/grupp8-fangdjupet/pulls) → kugghjulet vid **Assignees** → välj personen.

   Alternativ i IntelliJ: *Git → GitHub → Create Pull Request*. Eller i terminalen med GitHub CLI: `gh pr create --base main --fill --assignee @me --reviewer <användarnamn>`.

### Granska och merga

1. GitHub Actions (`.github/workflows/tester.yml`) kör `mvn test` automatiskt på varje PR och varje push till `main`. Mergea bara när kontrollen **Tester** är grön.
2. Granskaren läser koden under fliken **Files changed**, kommenterar vid behov och väljer **Review changes → Approve**.
3. Ska något ändras: fixa på samma branch, committa och pusha igen – pull requesten uppdateras automatiskt.
4. När den är godkänd: klicka **Merge pull request** → **Confirm merge** och sedan **Delete branch**.
5. Efter merge, hämta senaste koden lokalt:
   ```
   git checkout main
   git pull
   ```
   Ny uppgift = ny branch från uppdaterad `main`.

## Ansvar

| Modul              | Paket                  | Ansvarig                     |
|--------------------|------------------------|------------------------------|
| Strid & karaktärer | `se.fangdjupet.combat` | Robert (`elitrobban89`), A   |
| Föremål            | `se.fangdjupet.items`  | Tanaz (`TanazDaraee`), B     |
| Spelmotor & spara  | `se.fangdjupet.game`   | Fredrik (`KOFFEE439`), C     |

## Designmönster

### Strategy – hur man slåss (`combat`, Robert)
**Var:** `AttackStrategy` är ett interface med `calculateDamage(attacker, target)`. `MeleeAttack`,
`MagicAttack` och `RangedAttack` räknar skadan på var sitt sätt, och i #18 kom `KnowledgeAttack`,
`CleaningAttack` och `RandomAttack`. En `Character` *har* en strategi och kan byta den mitt i
striden med `setAttack(...)`: spelaren väljer attack varje runda, och en `Boss` byter till en
tyngre attack vid halva livet.

**Varför:** en ny attack blir en ny klass i stället för en ändring i `Character` eller `Battle`
(open/closed). Varje strategi går att testa för sig, och slumpen (`Random`) skickas in så att
testerna blir likadana varje gång. Alternativet, en `switch` på attacktyp i `Character`, hade
behövt ändras vid varje ny attack.

### Factory – hur föremål skapas (`items`, Tanaz)
**Var:** `ItemFactory` skapar vapen, rustningar och drycker utifrån `ItemType` och nivå:
`createWeapon(ItemType.BOW, "Pilbåge", 1)`. Fabriken väljer räckvidd och bonus, så den som anropar
behöver inte veta hur ett föremål byggs. Samma idé finns i `Martin.create()` och
`CleaningWitch.create(random)`, som bygger bossarna.

**Varför:** skapandet ligger på ett ställe. `WorldBuilder` och testerna ber om "en båge på nivå 1"
och får ett färdigt `Weapon`. Ändras reglerna för bonusen ändras bara fabriken.

### Observer – vem som får veta vad som händer (`game`, Fredrik)
**Var:** `GameEventListener` har en metod, `onEvent(String)`. `Battle` och `GameEngine` skickar varje
händelse dit, och `EventBus` skickar vidare till alla registrerade lyssnare, t.ex.
`ConsoleEventLogger` (skärmen). Testerna registrerar en lista i stället för konsolen.

**Varför:** den som skickar vet inte vem som lyssnar. Spelet skriver aldrig direkt till
`System.out`, så testerna kan läsa allt som händer, och en ny lyssnare – en loggfil eller en
topplista – läggs till utan att `Battle` eller `GameEngine` ändras.

## Git-samarbetet – reflektion

> **Utkast** – skrivet av Robert. Fredrik och Tanaz fyller i med egna rader under *Våra egna ord*
> innan inlämningen.

**Hur vi arbetade.** Dag 1 (26 okt) satte vi ihop kontraktet – de gemensamma interfacen,
`Character` och recorden – och lade in det i `main` via PR #2. Sedan hade var och en sitt paket och
sina egna `feature/...`-grenar. `main` har en regel: allt går via pull request och kräver ett
godkännande från någon annan, och ingen kan pusha direkt. CodeRabbit kommenterar varje PR
automatiskt, och från PR #9 kör GitHub Actions `mvn test` på varje PR.

**Konflikten vi löste.** Tors 29 okt mergades #7 (EventBus), #8 (ItemFactory), #9 (CI) och #10
(ASCII-grafik) samma dag. #8 ändrade recorden `Weapon`, `Armor` och `Potion`, och när konflikten
löstes blev både den gamla och den nya koden kvar i `ItemFactory.java`, `ItemType.java` och
`ItemFactoryTest.java`. `main` kompilerade inte, och CI blev röd. Vi tittade på det tillsammans och
lagade det i #11: Tanaz nya version behölls, den gamla togs bort, och A:s tester skrevs om till
de nya records-konstruktorerna.

**Det som fungerade bra.**
- Kontraktet först: B och C kunde börja direkt och bygga mot typerna innan A:s kod var klar.
- Regeln på `main` och granskningen: alla PR:ar har godkänts av någon annan innan merge.
- CI fångade fel som granskningen missade, t.ex. att `main` var röd efter #8 och efter #17
  (`EventBusTest` anropade `publish`, som hade bytt namn till `onEvent`).

**Det vi skulle göra annorlunda.**
- Merga mer sällan på samma dag, och dra in `main` i grenen (`git merge main`) och kör
  `mvn test` innan PR:en mergas – då hade #11 inte behövts.
- Läsa och svara på CodeRabbits kommentarer före merge. #15 mergades med tre anmärkningar kvar,
  och #16 fick fixa dem i efterhand.
- Fördela arbetet jämnare över personerna och veckorna, och öppna PR:ar tidigare i stället för
  stora PR:ar mot slutet.
- Slå på att CI måste vara grön för att få merga, inte bara att den körs.

**Våra egna ord.**
- *Robert:* …
- *Fredrik:* …
- *Tanaz:* …

## Användning av AI

Kontraktet (interfaces, abstrakt klass och records i `combat`, `items` och `game`) togs fram
med hjälp av AI dag 1. Målet var att komma igång snabbt: B och C bygger vidare på A:s kod,
så de gemensamma typerna behövde ligga i main från start för att alla tre skulle kunna jobba
parallellt.

Därefter har vi skrivit koden i våra egna paket själva, med AI som handledare: den har
förklarat begrepp (arv, `@Override`, Strategy, injicerad `Random`), pekat ut kompileringsfel
och föreslagit lösningar som vi sedan skrivit in och förstått. AI har också hjälpt till att
skriva tester och kommentarer. Varje PR har granskats av en gruppmedlem innan merge.

## Planering

| | Datum |
|---|---|
| Dag 1 (kontraktet) | mån 26 okt |
| A:s PR 1 helst klar | ons 28 okt |
| Redovisning | tors 12 nov |
| Sista inlämning | sön 15 nov |

### Vecka 1 (v.10, 26–30 okt) – uppskattad tid per roll

Grov uppskattning om man kodar själv: tester, PR och granskning av någon annans PR ingår.

| Roll | Vecka 1 | Tid |
|---|---|---|
| **A – Strid** | PR 1: logiken i `Character` + `Hero` och `Enemy` med tester (högst prio – B och C bygger på den) | 4–6 h |
| | PR 2 (start): `MeleeAttack`, `MagicAttack`, `RangedAttack` med tester | 3–4 h |
| | **Summa** | **≈ 7–10 h** |
| **B – Föremål** | PR 1: `ItemFactory` med tester | 2–3 h |
| | PR 2 (start): `Inventory` (streams, generics) och `ItemText` | 4–6 h |
| | **Summa** | **≈ 6–9 h** |
| **C – Motor** | PR 1: `EventBus`, `ConsoleLogger`, `FileLogger` med tester | 3–4 h |
| | PR 2 (start): `GameState`, `SaveManager`, `SaveFileException` med tester | 4–6 h |
| | **Summa** | **≈ 7–10 h** |

Räkna med ungefär 1 h extra per person för merge-konflikter och granskning.

## Hur kontraktet fungerar (kod)

Kontraktet är de gemensamma typerna som alla bygger mot – bara signaturer och records, ingen logik. Ett interface **implementeras** (`implements`), en klass **ärvs** (`extends`).

```
AttackStrategy (interface)          Character (abstrakt klass)
 ├─ MeleeAttack    A, PR 2           ├─ Hero    A, PR 1
 ├─ MagicAttack    A, PR 2           └─ Enemy   A, PR 1
 └─ RangedAttack   A, PR 2                └─ Boss  (frivilligt)
        ▲
        └──── Character HAR en AttackStrategy (fältet `attack`)

Item (sealed interface)             GameEventListener (interface)
 ├─ Weapon   record                  ├─ EventBus            C, PR 1
 ├─ Armor    record                  ├─ ConsoleEventLogger  C, PR 1
 └─ Potion   record                  └─ FileLogger          C (inte klar)
```

### `combat/AttackStrategy` – hur man slåss
En metod, `calculateDamage(attacker, target)`, som varje strategi räknar på sitt sätt:
- `MeleeAttack` – styrka + anfallarens bonus − målets försvar.
- `MagicAttack` – fast kraft, rustningen påverkar inte.
- `RangedAttack` – slumpar med `Random` (20 % miss), annars lägre skada.

Strategy-mönstret: attacken går att byta mitt i striden med `setAttack(...)`. `Hero.equip(Weapon)` gör just det utifrån vapnets räckvidd: båge (`RANGED`) ger `RangedAttack`, svärd (`MELEE`) ger `MeleeAttack` och spjut (`BOTH`) behåller attacken hjälten redan har. Med bara en abstrakt metod räcker även en lambda: `(a, t) -> 5`. `name()` har en `default`-kropp och behöver inte skrivas.

### `combat/Character` – vem som slåss
Abstrakt – man skapar en `Hero` eller `Enemy`, aldrig en `Character`. Subklasserna måste skriva:
- `getAttackBonus()` – Hero: från vapnet. Enemy: från nivån.
- `getDefense()` – Hero: från rustningen. Enemy: från nivån.

`Character` **är** ingen `AttackStrategy`, den **har** en (komposition). `attack(target)` frågar strategin om skadan.

### `items/Item` + `Weapon`, `Armor`, `Potion` – föremålen
`sealed ... permits` betyder att exakt de tre recorden får vara ett `Item`; en fjärde stoppas av kompilatorn. Records är ren data – Java skriver konstruktor och getters. Här kopplas B:s föremål in i A:s strid: `Hero.equip(Weapon)` ger `getAttackBonus()`.

### `game/GameEventListener` – vem som får veta vad som händer
Observer-mönstret: A:s `Battle` anropar `onEvent("Hjälten gör 7 i skada")` utan att veta vem som lyssnar. C:s `EventBus` skickar vidare till alla registrerade lyssnare, t.ex. `ConsoleEventLogger` (skärm) och senare `FileLogger` (fil).

### Varför kontraktet finns
Alla kan bygga parallellt från dag 1: B skriver `new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1)` och C skickar händelser innan A:s kod är klar. Bryter någon mot kontraktet kompilerar inte koden – felen syns direkt, inte när allt kopplas ihop. Ändras kontraktet senare sker det via en egen liten PR som alla granskar.

## Kopplingspunkter

Där modulerna möts. **Bestämt** = står i kontraktet och ändras bara via en egen PR. **Förslag** = bestäms i PR:en där koden skrivs – skriv beslutet i PR-beskrivningen och uppdatera tabellen.

| Koppling | Vem | Bestämt i kontraktet | Förslag – bestäms i |
|---|---|---|---|
| Utrustning påverkar striden | A ↔ B | `getAttackBonus()` och `getDefense()` i `Character`; `Weapon.damageBonus()`, `Armor.defense()` | `hero.equip(Weapon)` / `hero.equip(Armor)` – A:s PR 1. Vapnets räckvidd väljer attacken: `RANGED` → `RangedAttack`, `MELEE` → `MeleeAttack`, `BOTH` behåller den gamla – #12 |
| En dryck läker | A ↔ B | `Character.heal(int)`, `Potion.healAmount()` | `hero.drink(Potion)` anropar `heal(potion.healAmount())`, aldrig över max – A:s PR 1 |
| Fiender skapas | B ↔ A | `Character(name, maxHp, attack)` är `protected` | `Enemy(name, maxHp, attack, level)`; B:s `EnemyFactory.create(type, level)` bygger den – A:s PR 1 och B:s PR 3 |
| Vad en strid returnerar | A ↔ C | `attack(target)` returnerar skadan, drar inte av hp | `Battle.fight(hero, enemy)` returnerar vinnaren (`Character`) – A:s PR 3 |
| Händelser når loggen | A ↔ C | `GameEventListener.onEvent(String)` | `Battle` får en `GameEventListener` i konstruktorn; C:s `EventBus` skickas in – A:s PR 3 |
| Inventariet sparas | B ↔ C | alla föremål har `type()`, `description()` och `level()` | sparas som `sword;3`; B lägger till en metod som läser tillbaka raden (gamla `ItemFactory.create("sword", 3)` togs bort i #8) – B:s nästa PR och C:s PR 2 |
| ASCII-grafik i konsolen | A ↔ C | `GameEventListener.onEvent(String)` – all utskrift, även grafiken, går via lyssnaren och aldrig direkt till `System.out` | Bara ren ASCII (`#` `-` `\|` `/`), ingen JavaFX/Swing och inga nya beroenden. A:s `HpBar.render(character, width)` ger `[#####-----]` för 20/40 hp och `HpBar.status(...)` hela raden `Troll  [#######---] 28/40` för `Battle`-loggen. C:s `AsciiArt` har `TITLE`, `MARTIN`, `FRIDA`, `banner(text)` och `show(listener, art)`. Mergat i #10; rumskartan kommer med C:s `World` |
| Sparfilen | A, B, C | – | en rad per sak, fält åtskilda med `;`: `hero;<namn>;<hp>`, `room;<rummets namn>`, en rad per föremål – C:s PR 2, alla tre granskar |
