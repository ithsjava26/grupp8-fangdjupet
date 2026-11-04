# grupp8-fangdjupet

**Fångdjupet** är ett textbaserat rollspel i konsolen, byggt i Java av tre personer i kursen
Javaprogrammering (grupp 8). Hjälten tar sig igenom skolan rum för rum – Skoldörren, Kafferummet,
Korridoren och städskrubben och till sist Lektionssalen – och slåss mot fiender som JavaFläcken,
ett troll och en trollkarl, StädHäxan och slutbossen Lektor Martin. Spelaren väljer attack varje
runda, plockar upp vapen och får hjälp av kaffe, kanelbullar och Frida. All grafik ritas med tecken i konsolen.

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
4. När den är godkänd: klicka **Squash and merge** → **Confirm squash and merge** och sedan **Delete branch**. `main` tillåter bara squash, så PR:en blir en commit där.
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

Paketet `se.fangdjupet.sound` (musik och ljud) kom till i #22. Det är ett extrapaket utöver de tre, byggt av Robert.

## Designmönster

### Strategy – hur man slåss (`combat`, Robert)
**Var:** `AttackStrategy` är ett interface med `calculateDamage(attacker, target)`. `MeleeAttack`,
`MagicAttack` och `RangedAttack` räknar skadan på var sitt sätt, och i #18 kom `KnowledgeAttack`,
`CleaningAttack` och `RandomAttack`, i #22 `JavaBookAttack` och `StressBallThrow`, och i #23 `UsbVirusAttack` och `LavaLampSmash`. En `Character` *har* en strategi och kan byta den mitt i
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
`ConsoleEventLogger` eller `AnimatedConsoleLogger` (skärmen) och `FileLogger` (loggfilen). Testerna registrerar en lista i stället för konsolen.

**Varför:** den som skickar vet inte vem som lyssnar. Spelets händelser går aldrig direkt till
`System.out` (bara menyerna, kartan och frågorna skrivs med `IO.println`), så testerna kan läsa allt som händer, och en ny lyssnare – en loggfil eller en
topplista – läggs till utan att `Battle` eller `GameEngine` ändras. Så kom ljudet till i #22: `sound/SoundListener` är bara en lyssnare till på `EventBus`, som spelar musik och ljud efter raderna den får.

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
- Slå på redan från början att CI måste vara grön för att få merga, inte bara att den körs. Det kom på plats först 4 nov.

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
| | PR 2 (start): `GameState` och `GameSaveFileHandler` – mergad som #15 | 4–6 h |
| | **Summa** | **≈ 7–10 h** |

Räkna med ungefär 1 h extra per person för merge-konflikter och granskning.

## Hur kontraktet fungerar (kod)

Kontraktet är de gemensamma typerna som alla bygger mot – bara signaturer och records, ingen logik. Ett interface **implementeras** (`implements`), en klass **ärvs** (`extends`).

```
AttackStrategy (interface)          Character (abstrakt klass)
 ├─ MeleeAttack    A, PR 2           ├─ Hero    A, PR 1
 ├─ MagicAttack    A, PR 2           └─ Enemy   A, PR 1
 ├─ RangedAttack   A, PR 2                └─ Boss  A – Martin.create(), CleaningWitch.create()
 │   └─ CleaningAttack  StädHäxans kast (Vanish går igenom rustningen)
 ├─ KnowledgeAttack A, PR 3 (Dela skärm: +12 hp)
 ├─ JavaBookAttack  citera kapitel 18 / 20 (14, slinter 30 %)
 ├─ StressBallThrow kasta stressbollen (10)
 ├─ UsbVirusAttack  20 % chans att vinna direkt, ett försök
 └─ LavaLampSmash   krossa Martins lavalampa (ingen skada)
        ▲
        └──── Character HAR en AttackStrategy (fältet `attack`)

AttackChooser (interface)   A – spelaren väljer attack per runda
Frida (ingen Character)     A – ger två drycker före Martin

Item (sealed interface)             GameEventListener (interface)
 ├─ Weapon   record                  ├─ EventBus            C, PR 1
 ├─ Armor    record                  ├─ ConsoleEventLogger  C, PR 1
 └─ Potion   record                  ├─ FileLogger          C – fangdjupet.log
                                     ├─ AnimatedConsoleLogger  konsolen: filmer och färger (ConsoleStyle)
                                     └─ SoundListener       sound/ – musik och ljud (Speaker → RetroSpeaker)
```

### `combat/AttackStrategy` – hur man slåss
En metod, `calculateDamage(attacker, target)`, som varje strategi räknar på sitt sätt:
- `MeleeAttack` – styrka + anfallarens bonus − målets försvar.
- `MagicAttack` – fast kraft, rustningen påverkar inte.
- `RangedAttack` – slumpar med `Random` (20 % miss), annars lägre skada.
- `KnowledgeAttack` – fast kraft som ignorerar försvaret, som `MagicAttack`, men vissa attacker kostar anfallaren hp (`DOOMSCROLL`, `STACKOVERFLOW`). `SCREENSHARE` (Dela skärm) gör ingen skada: hjälten tar över lektionen och får tid, **+12 hp** (`KnowledgeAttack.healing`, `selfHeal()`), och den missar aldrig (`missPercent()` 0). En chansning mellan två värden går fortfarande att göra med `KnowledgeAttack(namn, låg, hög, Random)`.
- `CleaningAttack extends RangedAttack` – StädHäxans kast. Samma uträkning som pilbågen (20 % miss, annars styrka + halva bonusen − försvaret), men med eget namn och verb: `SPONGE` *kastar en tvättsvamp på* (7), `SVINTO` *kastar Svinto på* (9) och `CHEMICALS` *sprayar kemikalier på* (11). `VANISH` *sprejar Vanish i ansiktet på* (10) går **rakt igenom rustningen**: `throughArmor()` är sant, och `defenseOf` (som `RangedAttack` frågar efter målets försvar) ger då 0.
- `RandomAttack(random, attacker...)` – en strategi gjord av andra strategier. Varje slag slumpar den fram en av dem, och `name()`, `verb()` och `selfDamage()` hör till den som valdes senast, så loggen stämmer med slaget.

Två `default`-metoder utöver `name()` gör att en strategi bara skriver det den vill ändra:
- `selfDamage()` – hur mycket attacken kostar anfallaren. Förvalt 0. `Battle` drar av den efter varje slag.
- `verb()` – ordet som loggen använder: förvalt `"slår"`, `RangedAttack` och `MagicAttack` skriver över det med `"skjuter"`.

Strategy-mönstret: attacken går att byta mitt i striden med `setAttack(...)`. `Hero.equip(Weapon)` gör just det utifrån vapnets räckvidd: båge (`RANGED`) ger `RangedAttack`, staff (`MAGIC`) ger `MagicAttack`, svärd (`MELEE`) ger `MeleeAttack` och spjut (`BOTH`) behåller attacken hjälten redan har. Med bara en abstrakt metod räcker även en lambda: `(a, t) -> 5`. `name()` har en `default`-kropp och behöver inte skrivas.

### `combat/Battle` – hur en strid går till
`new Battle(listener).fight(hero, enemy)` kör striden och returnerar vinnaren. Med `new Battle(listener, chooser)` frågar `Battle` en `AttackChooser` före varje hjältens slag; den returnerar en `AttackStrategy` (som blir hjältens nya attack) eller `null` för att behålla den nuvarande. Så väljer spelaren attack varje runda i `GameEngine`. Reglerna:
- **Turordning:** hjälten slår först, sedan fienden. Den som är död slår inte tillbaka.
- **Attackval:** valet görs i början av varje runda, före hjältens slag. Den valda attackens `selfDamage()` räknas som vanligt.
- **Attackens kostnad:** efter varje slag dras `selfDamage()` av anfallaren. Hjälten kan alltså dö av sin egen attack.
- **Dör båda i samma slag** vinner den som slog: dödar hjälten fienden (och dör själv) vinner hjälten, dödar fienden hjälten (och dör själv) vinner fienden.
- **Rundtak:** efter 100 rundor avbryts striden. Då vinner den med mest hp kvar, och vid lika hp vinner fienden.
- **Hp återställs inte** mellan strider.
- **Slump:** med `new Battle(listener, chooser, random)` missar varje slag med 10 % chans (`Hero: Robert missar Troll!`), och en träff gör ±1 i skada, aldrig under 0. En miss (0 i skada) förblir 0. Det gäller både hjälten och fienderna. `GameEngine` kör striderna så. Utan `Random` blir skadan exakt, och så kör testerna.
- **Flera fiender:** `fight(hero, List<Enemy>)`. Varje runda slår hjälten den första fienden som lever, sedan slår varje levande fiende.
- **Loggen:** varje slag skickas som text till `GameEventListener.onEvent`, till exempel `Hero: Robin skjuter Troll skada: 5`, och sist `Robin vinner mot Troll!`. Ett slag som gör 0 i skada (ett bomkast, eller för bra försvar) skrivs som `Enemy: StädHäxan kastar Svinto på Robert men missar!`. `Battle` skriver aldrig direkt till konsolen. `Robin vinner Fångdjupet!` skriver `GameEngine` bara när hjälten besegrar Martin, den sista bossen i Lektionssalen.
- **Grafik:** efter varje träff loggas den träffades hp som ASCII-mätare via `HpBar.status(character, 10)`, till exempel `Troll  [######----] 6/10`. Den som dör visar en tom mätare (`[----------] 0/10`). En attack som kostar anfallaren hp (`selfDamage`) ger också en mätare för anfallaren.

### Rum, vapen och grafik i spelet
- `GameEngine` kör striden med `Battle` (spelmenyn 1) och frågar spelaren vilken attack som ska användas varje runda (Enter behåller den nuvarande). Menyn kommer från `game/AttackMenu`: `options(hero)` ger närstrid, magi och avstånd – eller, när hjälten har Java-boken, Martins fyra `KnowledgeAttack` och **5. Citera från Java-boken** (`JavaBookAttack`, en per hjälte via `Hero.getJavaBookAttack()`): första gången *kapitel 18: Polymorphism*, nästa runda *kapitel 20: Interface*, sedan från början igen. 14 i skada, kostar ingenting men har 30 % risk att slinta (StackOverflow gör 11 men kostar 3 hp). Loggen visar kapitlet: `Hero: Robert citerar kapitel 18: Polymorphism för Lektor Martin skada: 14`, och bilden är boken uppslagen på rätt kapitel (`AttackArt.POLYMORPHISM`, `AttackArt.INTERFACE`). `lines(...)` ger menyraderna och `choose(options, inmatning)` gör om det spelaren skrev till en attack (`null` = behåll). Den går att testa utan tangentbord.
- **Hjälten börjar med ett svärd** (`GameEngine.newHero`): `Du har Svärd i handen: +1 i närstrid.` Svärdet syns under Visa utrustning, och närstriden blir **5 + 1 = 6** (`AttackMenu.MELEE`, `SWORD_BONUS`). I början finns bara `1. Närstrid` i attackmenyn: **magi och avstånd låses upp** först när man plockar upp staven eller pilbågen (`Hero.hasPickedUp`), och finns sedan kvar även när ett annat vapen tar platsen i handen – fast svaga (4) utan vapnet. Attacken för vapnet i handen är **gulmarkerad** i attackmenyn (`AttackMenu.lines(hero, options)`), t.ex. `2. Avståndsattack` med pilbågen. Svärdet räcker inte: 4000 simulerade spel (från före den svårare balansen) med bara svärdet gav **4 %** vinster (85 % dör mot Trollet och Trollkarlen), med pilbågen och staven **76 %** vinster och **89 %** fram till Martin.
- **Vapnen gör skillnad** (`AttackMenu`, `Hero.getAttackBonus`): svärdet är svagt. Pilbågen gör avståndsattacken till **13** + halva bågens bonus (`BOW_RANGED`, 20 % miss), och Trollstaven gör magin till **8 + stavens bonus** (`STAFF_MAGIC`, går rakt igenom rustningen). Utan vapnet i handen är magi och avstånd bara 4. Vapnets bonus hjälper bara sin egen attack – en pilbåge gör inte närstriden starkare (spjutet hjälper både närstrid och avstånd). Den som plockar upp pilbågen eller staven byter direkt till vapnets attack (`GameEngine.equipAndAim`, `AttackMenu.forWeapon`): `Du byter till Avståndsattack.` – Java-boken mot Martin går före. Simulerat 4000 gånger fram till Martin (kaffet och kanelbullen tagna): med pilbågen mot Trollet och Trollkarlen och staven mot StädHäxan tar man sig förbi **88 %**, med bara knytnävarna (närstrid 7, före svärdet) **36 %** (förut var det tvärtom: 94 % med närstrid och 3–9 % med pilbågen).
- **Två menyer** (`GameEngine.menu(room)`): utan äventyr visas **huvudmenyn** (1. Starta nytt äventyr, 2. Fortsätt sparat äventyr, e. Avsluta). I spelet visas **spelmenyn** med rummets namn som rubrik (1. Strid mot rummets fiende, 2. Gå till ett annat rum, 3. Visa hjältens utrustning, 4. Spara äventyret, 5. Leta i rummet, e. Avsluta). Efter game over och efter vinsten kommer huvudmenyn tillbaka.
- **Gå till ett annat rum – spelaren väljer** (spelmenyn 2): spelet visar kartan och rummen med siffror, och spelaren väljer vart (Enter = stanna kvar):
  ```
  Vart vill du gå?
  1. Skoldörren  (låst)
  2. Kafferummet  (du är här)
  3. Korridoren och städskrubben  (nytt rum)
  4. Lektionssalen  (låst)
  Välj rum (Enter = stanna kvar):
  ```
  Man kan gå tillbaka till alla rum man har varit i, och vidare till nästa nya rum när rummet längst fram är rensat (`World.isOpen`, `World.moveTo`). **Inte förbi en levande fiende:** ett låst rum ger `Kafferummet är låst. Entrédörren står i vägen i Skoldörren – besegra rummets fiender först.` (`GameEngine.lockedMessage`, som använder `blockedBy(room)`). Det gäller också efter rundtaket, där hjälten kan vinna på mest hp fast fienden lever. Rum längre bort: `Lektionssalen är låst. Du har inte hittat vägen dit än.`
- **Kartan syns hela tiden** (`GameEngine.roomMap(world)`), ovanför spelmenyn och i "Gå till ett annat rum". Rummen ligger som en slinga, med streck i ren ASCII och namnen med å, ä och ö:
  ```
  KARTA  @ du är här  x rensat  ! fiender  ? nytt  # låst
  [#] Skoldörren    -- [@] Kafferummet
                        |
  [#] Lektionssalen -- [#] Korridoren
  ```
  `@` = här, `x` = rensat, `!` = varit där och fiender lever, `?` = öppet men nytt, `#` = låst. Kartan skrivs med `IO.println` som menyn, så den hamnar inte i loggfilen.
- **Spara när man gått tillbaka:** sparningen tar rummet **längst fram** som hjälten har nått (`World.getFurthestRoomIndex()`), inte det hen står i. Rummen före är ju rensade.
- **Inlåst i Fångdjupet:** när hjälten går in i skolan (från Skoldörren till Kafferummet) slår Entrédörren igen bakom hen (`AsciiArt.LOCKED_DOOR`): `Entrédörren slår igen bakom dig!` och `Du är nu inlåst i Fångdjupet och behöver besegra slutbossen innan du kan ta dig ut.` Skoldörren är sedan låst på kartan (`#`) och i rumsvalet tills Martin i sista rummet är besegrad (`World.isEntranceLocked`). Det gäller också när en sparning läses in.
- **Skolbyggnaden ovanför huvudmenyn:** `AsciiArt.SCHOOL` – Fångdjupets skola med klocktorn, flagga, fönster och entrédörr – visas bara ovanför huvudmenyn (starta, fortsätt, avsluta), inte i spelmenyn.
- **Gulmarkerade menyval:** lever en fiende i rummet blir `1. Strid mot rummets fiende  (Entrédörren väntar!)` gult (`GameEngine.fightOption`), och finns något att hitta – något man lämnat kvar eller något gömt – blir `5. Leta i rummet  (något finns här!)` gult (`GameEngine.searchOption`, `Room.hasSomethingToFind`). Att leta erbjuder först det som ligger kvar (j/n), sedan det gömda.
- **Visa hjältens utrustning** (spelmenyn 3, `GameEngine.equipment(hero)`): hp-mätaren, vapnet, rustningen, attacken, Java-boken, stressbollen och ryggsäcken (`Hero.getInventory()`, allt hjälten plockat upp, också vapen som bytts ut) – och sedan **bilder på allt hjälten bär**: vapnet, rustningen, Java-boken och stressbollen. Till exempel:
  ```
  Robert  [########--] 34/40
  Vapen: Pilbåge (+3 i skada)
  Rustning: ingen
  Attack: Närstrid
  ```
- **Leta i rummet** (spelmenyn 5, `Room.withHiddenItem` / `Room.search()`): gömda föremål hittas bara om man letar, och bara en gång. I **städskrubben** (Korridoren och städskrubben) ligger **Städrustningen** – gummiförkläde, hink som hjälm och moppen – med **+20 i försvar** (`Du tar på dig Städrustning: +20 i försvar.`). Med den gör närstrid och StädHäxans kast nästan ingen skada, men Martins kunskap går rakt igenom rustningen – och **Martin blir argare** när han ser den (`Martin.enrage`, `Boss.enrage`). Då ser han att du inte kan något i Java, slumpat ur `Martin.CANT` (`Martin.angryLine(random)`): `Lektor Martin ser att du inte kan skriva en konstruktor och blir arg! Han förklarar hårdare.` (eller *jämför strängar med ==*, *glömmer break i switchen*, *har gjort alla fält public* …) och repliken *"Städrustning? Den implementerar inte Shield-interfacet – kunskap kompilerar rakt igenom!"*. Hans första förklaring gör då 8 i skada i stället för 7 (fas 2 är kvar på 15). **Stressbollen gör honom också arg**: har hjälten den i fickan när striden börjar säger han `Lektor Martin ser stressbollen och blir arg! Han förklarar hårdare.` och *"En stressboll? Stress är en checked exception – den får du hantera själv!"* (`Martin.enrage(boss, Martin.STRESS_BALL_LINE)`). Han blir bara arg en gång – stressbollen går före rustningen. **StädHäxan** – det är ju hennes rustning – skrattar elakt i stället (`CleaningWitch.enrage`): `StädHäxan ser sin Städrustning på dig och skrattar elakt: "Mwahahaha!"` och *"Jaså, min städrustning? Akta dig så jag inte kastar Svinto på dig – och tvättsvampar! Och sen blir det kemikalieangrepp: jag sprejar dig i ansiktet med Vanish!"* Vid halva livet blir kemikalierna `CleaningAttack.VANISH` (`sprejar Vanish i ansiktet på`, egen bild) – förklädet skyddar inte ansiktet, så Vanish går **rakt igenom rustningen**. Hennes kast studsar fortfarande mot förklädet. Andra bossar bryr sig inte (`GameEngine.reactToCleaningArmor`). I de andra rummen: `Du hittar inget här.` Rustningen sparas i ryggsäcken. `ItemType` har ingen egen rustningssort, så rustningen är en `SHIELD`.
- **Stressbollen** (`items/StressBall`, `ItemType.STRESSBALL`): gömd i **Lektionssalen** – leta (spelmenyn 5) så hittar du den: `Du hittar: Stressboll!` och `Du stoppar Stressboll i fickan: i striden klämmer du den, +6 hp varje runda.` I början av varje runda klämmer hjälten den (`Battle` → `Hero.squeezeStressBall()`): `Robert klämmer stressbollen: +6 hp`. Den finns också som **6. Kasta stressbollen** i attackmenyn mot Martin (`combat/StressBallThrow`): `(10 i skada om den träffar – sedan är bollen borta: ingen +6 hp per runda)`. Träffar kastet är bollen borta och hjälten citerar ur boken nästa runda (Enter); missar det studsar bollen tillbaka (`Robert kastar stressbollen förbi Lektor Martin!`). Bollen har en egen bild, kastet en egen attackbild (`AttackArt.STRESS_BALL_THROW`: bollen i en båge, *SQUISH!*), och bollen syns i utrustningen: `Stressboll: i fickan (+6 hp per runda, eller kasta den på Martin)`. Den sparas i ryggsäcken, som Städrustningen.
- **Bilder för föremålen:** `AsciiArt.forItem(item)` tar först bilden på namnet (Pilbåge, Trollstav, kaffet, Städrustningen) och annars på sorten: svärd, spjut, båge, stav, sköld, hjälm och dryck. Den visas när man plockar upp eller hittar något och under Visa hjältens utrustning.
- **Spara och fortsätta:** spelmenyn 4 sparar hjältens namn, maxHp, hp, rummets nummer och ryggsäcken på sparplats 1 i mappen `saves` (`Äventyret är sparat: Kafferummet, 34/40 hp.`). En gammal sparning skrivs över. Huvudmenyn 2 läser in den: `Välkommen tillbaka, Robert!`, och hjälten står **i början av det sparade rummet** med samma hp. Rummen före är avklarade och fienderna där kommer inte tillbaka. Hjälten har kvar allt i ryggsäcken: vapnet i handen med samma bonus, rustningen och stressbollen (`GameEngine.heroFrom`). Det hjälten redan har försvinner ur rummen, så Städrustningen går inte att hitta två gånger, och det hjälten lämnade ligger kvar (`GameEngine.resume`). En gammal sparfil utan föremål går fortfarande att läsa: då hämtas vapnen från rummen före (`GameEngine.fastForward`). Finns ingen sparning skriver spelet `Det finns inget sparat äventyr. Välj 1 för att starta ett nytt.` En trasig sparfil ger `Sparfilen går inte att läsa: ...` och spelet fortsätter. Drycker ligger aldrig i ryggsäcken. En stressboll som kastats finns inte i sparningen, men rummet börjar om, så den går att hitta igen i Lektionssalen.
- **Loggfil:** `Main` registrerar en `FileLogger` på `EventBus`, så allt som händer också skrivs till `fangdjupet.log`. Färgkoderna runt GAME OVER tas bort i filen. Går filen inte att skriva fortsätter spelet ändå. `saves/` och `fangdjupet.log` ligger i `.gitignore`.
- **Föremål i rummet – spelaren väljer:** det som ligger i rummet **gulmarkeras** (`AsciiArt.yellow`, ANSI `ESC[33m`) med bild och bonus, och spelet väntar på svar: `Här ligger: Pilbåge (+3 i skada)` och `Plocka upp Pilbåge? (j/n):`. `j` (eller `ja`, `y`) plockar upp (`GameEngine.wantsIt`), allt annat lämnar det kvar: `Du låter Pilbåge ligga kvar. Den finns kvar om du kommer tillbaka.` – och frågan kommer igen när man går tillbaka till rummet. Java-boken tas fortfarande automatiskt (mot Martin hjälper bara kunskap). När en sparning läses in tar hjälten med sig vapnen från rummen före (`GameEngine.pickUpAll`). Ett vapen hamnar i handen, ger sin bonus i skada och hjälten byter till vapnets attack (`Du har nu Pilbåge i handen: +4 i skada.` och `Du byter till Avståndsattack.`).
- `AsciiArt.forRoom(rumsnamn)` ger en ASCII-bild för Skoldörren, Kafferummet (kaffeautomaten med JAVA-skylten), Korridoren och städskrubben och Lektionssalen (tom `Optional` för okända rum). `showCurrentRoom()` visar den, och dessutom det som ligger i rummet (gulmarkerat, med bild).
- `AsciiArt.forName(namn)` ger en bild för fiender, saker och personer: Entrédörren ("Har du passerkort?"), JavaFläcken, Troll, Trollkarl, StädHäxan, Lektor Martin, Frida, kaffekoppen, kanelbullen, Java-boken, pilbågen och trollstaven. Den visas när fienden dyker upp, när man tar kaffet eller kanelbullen, när Frida kommer och när man plockar upp något.
- **Bilder för attackerna:** `combat/AttackArt.forAttack(attack)` ger en bild för varje attack i spelet – närstrid (svärd), magi, avstånd (pil), DoomScrolla (mobilen som scrollar), JavaQuizet, StackOverflow, Dela skärm, Martins två förklaringar och StädHäxans tvättsvamp, Svinto och kemikalier. Bilden hittas först på attackens namn, sedan på typen – `CleaningAttack` ärver `RangedAttack` men får StädHäxans bild, inte pilen. `Battle` visar bilden när spelaren väljer en attack (Enter = behåll visar ingen), och första gången en boss använder en attack i striden. Det slås på med `new Battle(...).withAttackArt()` – av som standard, så att stridstesterna kan räkna loggraderna.
- **Vinst:** när Martin är besegrad visar `GameEngine` `AsciiArt.VICTORY` (en pokal och *D U   V A N N !*) före `Robert vinner Fångdjupet!`.
- **Flera fiender i ett rum:** `Room` kan ta en lista med fiender. `getEnemy()` ger den första som lever och `null` när rummet är rensat. Vinner hjälten och rummet har fler fiender skriver spelet `Men vänta... Troll har gömt sig i Kafferummet och hoppar fram!`, och nästa strid (menyval 1) är mot den.
- **Flera fiender samtidigt:** `room.thenTogether(troll, wizard)` lägger till en våg där fienderna slåss tillsammans. `room.getEnemies()` ger den aktuella vågen och `Battle.fight(hero, List<Enemy>)` kör striden. Varje runda slår hjälten den första fienden som lever, och sedan slår varje levande fiende i tur och ordning. Slutraden blir `Robert vinner mot Troll och Trollkarl!`.
- **Paus mellan vågorna:** `room.withRefreshment(replik, dryck)` ger något att dricka när första vågen är besegrad, bara en gång. `GameEngine.nextWaveIsReady` kollar att hela vågen verkligen är död – vid rundtaket kan hjälten vinna på mest hp fast fiender lever, och då kommer varken kaffet eller nästa våg. I Kafferummet: `Du ser dig om efter en kaffe... Där! Kannan på bänken är fortfarande varm.` och `Robert tar en kopp kaffe (+11 hp)`. Kaffet ger 30, men siffran är det hjälten faktiskt fick (aldrig över max).
- **Belöning när rummet är rensat:** `room.withReward(replik, dryck)`, bara en gång. I Kafferummet: `På bänken bredvid kannan ligger en kanelbulle. Den har ingen tagit!` och `Robert tar en kanelbulle (+27 hp)`. Kanelbullen ger 35.
- **Kaffet och kanelbullen – spelaren väljer:** vid rätt ögonblick (kaffet när JavaFläcken är besegrad, kanelbullen när rummet är rensat) gulmarkeras repliken och bilden, hjältens hp visas och spelet väntar på svar: `Ta en kopp kaffe (+30 hp)? (j/n): ` (`GameEngine.offerRefreshment`, `takeQuestion`). Säger man nej står den kvar i rummet (`Room.leaveItem`): `Du låter en kopp kaffe stå kvar. Den finns kvar om du letar i rummet eller kommer tillbaka.` – men Trollet och Trollkarlen hoppar fram direkt, så den som tackar nej kan dö.
- **Repliker:** `new Enemy(...).withLine("...")` ger fienden något att säga. Repliken skrivs ut när fienden dyker upp, t.ex. `Trollkarl: "Thou shall not pass!"`.
- **Ingen "Prata med någon" i menyn:** karaktärerna pratar av sig själva när de dyker upp – fienderna med sina repliker och Frida när StädHäxan är besegrad. Därför finns inget sådant menyval.
- **Game over:** dör hjälten skriver `GameEngine` GAME OVER i stora röda bokstäver (`AsciiArt.GAME_OVER` i `AsciiArt.red(...)`, ANSI-färgkoden `ESC[31m`), vem som vann – eller att hjälten dog av sin egen attack – och att 1 i huvudmenyn startar om. Äventyret nollställs och huvudmenyn kommer tillbaka. Färgen syns i IntelliJ och Windows Terminal; bilden i sig är ren ASCII.

#### Rummen och fienderna

| Rum | Fiender (i tur och ordning) | Föremål |
|---|---|---|
| Skoldörren | Entrédörren – tung dörr, **20 hp, närstrid 6** (7 per slag med nivån, fyra svärdshugg) | – |
| Kafferummet | JavaFläcken – seg fläck, **24 hp, närstrid 7, nivå 2** (två pilar, och den slår tillbaka med 9). Sedan en **kaffe** från kaffeautomaten (+30 hp, aldrig över max). Sist de riktiga fienderna som gömt sig där, **samtidigt**: **Troll** (12 hp, **närstrid 9**, nivå 2 – 11 per slag) och **Trollkarl** (**24 hp, magi 7**, nivå 2, säger *"Thou shall not pass!"*). När rummet är rensat: en **kanelbulle** (+35 hp). | Pilbåge |
| Korridoren och städskrubben | **StädHäxan** (`CleaningWitch.create(random)`, en `Boss`) – 40 hp (`CleaningWitch.HP`), nivå 2. Kastar tvättsvamp (7) eller Svinto (9) (slumpas varje slag), och vid halva livet sprayar hon kemikalier (11). Alla tre är avståndsattacker. Hotar: *"Ett steg till mot lektionssalen och jag tar fram kemikalierna!"* När hon är besegrad dyker **Frida** upp (se nedan). | Trollstav (staff) |
| Lektionssalen | **Lektor Martin** (`Martin.create()`, en `Boss`) – 100 hp, nivå 3. Förklarar 7 per slag, vid halva livet **15** – mer än dubbelt så hårt. Den sista bossen: besegrad = `vinner Fångdjupet!` | Java-boken (tas upp direkt) |

#### Hjältens hp genom spelet (körning utan slump i `Battle`, närstrid och StackOverflow mot Martin – från före vapenbalansen, då närstriden var 10)

| Efter | Robert |
|---|---|
| Entrédörren | 34/40 |
| JavaFläcken | 29/40 |
| Kaffe | 40/40 |
| Troll och Trollkarl | 13/40 |
| Kanelbulle | 40/40 |
| StädHäxan | 9–11/40 om inget av hennes kast missar (i en riktig körning 18/40) |
| Fridas två drycker | 40/40 – full hälsa mot Martin |
| Lektor Martin | 9/40 – **vinner Fångdjupet!** |

#### Svårighet med slumpen
1000 simulerade spel med samma val (närstrid med vapnen i handen, sedan StackOverflow mot Martin, före vapenbalansen): **795 vinster**. Förlusterna: 51 mot trollet och trollkarlen, 149 mot Martin (37 av dem av StackOverflows egen kostnad) och 5 mot StädHäxan. Om man bara delar skärm mot Martin vinner man 357 av 1000. Siffrorna varierar några procent mellan körningar.

#### `combat/Frida` – hjälp före Martin
Frida är den goda karaktären. Hon slåss inte och är därför ingen `Character`. `Frida.create()` skapar henne och `WorldBuilder` ställer henne i Korridoren och städskrubben med `room.withFrida(...)`. När rummets sista fiende (StädHäxan) är besegrad dyker hon upp:

```
Frida dyker upp!
Frida: "Du ser trött ut! Ta de här två innan du möter Martin."
Robert dricker Fridas hälsodryck (+20 hp)
Robert  [#######---] 30/40
Robert dricker Fridas hälsodryck (+10 hp)
Robert  [##########] 40/40
```

- `give()` ger två hälsodrycker (`Potion`, 20 hp var) första gången och en tom lista därefter. Gåvan kan alltså bara fås en gång.
- Hjälten dricker dem direkt med `hero.drink(...)`. 2 × 20 hp räcker till full hälsa (40) även från 1 hp, och `heal` går aldrig över `maxHp`.

### `combat/Boss`, `combat/CleaningWitch` och `combat/Martin` – bossarna
- `Boss extends Enemy` börjar med en fas 1-attack och byter till en tyngre fas 2-attack när hp når halva livet, bara en gång. Bytet sker i `takeDamage`, och en `heal` byter inte tillbaka. `isInPhaseTwo()` visar om bytet skett.
- `CleaningWitch.create(random)` ger StädHäxan som en `Boss`: fas 1 är en `RandomAttack` av `SPONGE` och `SVINTO`, och fas 2 (vid 20 hp) är `CHEMICALS`. **Svårare än förut** (40 hp i stället för 30, kasten 7/9 och kemikalierna 11 i stället för 6/8/10, och Vanish genom rustningen). Simulerat 20 000 gånger med pilbågen från full hp: utan rustning vinner hjälten **78 %** (förut 93 %), i Städrustningen **96 %** men med runt 15 hp mindre (förut alltid och utan en skråma, eftersom +20 i försvar tog bort allt).
- `Martin.create()` ger en `Boss`: Lektor Martin, **100 hp**, nivå 3. Han förklarar med `KnowledgeAttack`: *Thoroughly explained the Hello World Java to you* (**7**), och under halva livet *Utterly explained* (**15**, mer än dubbelt så hårt). I Städrustningen eller med stressbollen blir fas 1 8. I fas 2 går musiken upp i tempo: `bossFast` (228 bpm i stället för 168, en ton högre) i både konsolen och webben.
- `Martin.heroChoices()` ger hjältens fyra val mot honom, och boken är det femte. Attackmenyn visar vad varje val gör (`AttackMenu.effect`):

  | Val | Mot Martin |
  |---|---|
  | 1. DoomScrolla | 9 i skada, kostar dig 2 hp |
  | 2. Citera från JavaQuizet | 7 i skada |
  | 3. Citera StackOverflow | 11 i skada, kostar dig 3 hp |
  | 4. Dela skärm och ta över lektionen | +12 hp till dig, ingen skada, missar aldrig |
  | 5. Citera från Java-boken | **14** i skada – den bästa – men 30 % risk att du slinter på sidan och missar (`JavaBookAttack.missPercent()`, `missVerb()`) |

  Alla andra slag missar 10 % (`AttackStrategy.USUAL_MISS_PERCENT`), även Martins. Battle frågar attacken: `selfHeal()` > 0 ger hp i stället för skada (`Robert delar skärmen och tar över lektionen: +12 hp, ingen skada på Lektor Martin`), och `missPercent()`/`missVerb()` ger `Robert slinter på sidan och missar Lektor Martin!`.
- **Java-boken:** Lektionssalen har den (`room.withJavaBook()`, `takeJavaBook()` bara en gång). När man kommer in tar hjälten upp den med `hero.pickUpJavaBook()`, som byter attack till StackOverflow. `hero.hasJavaBook()` gör att `AttackMenu` bara visar de fyra.
- Martins porträtt (`AsciiArt.MARTIN`) och repliken *"Let me explain Hello World to you..."* visas när han dyker upp.
- **Martin förhör dig** (`Martin.QUESTIONS`, `Enemy.withTaunts` / `nextTaunt`): varje runda innan han förklarar ställer han en Java- eller databasfråga, i tur och ordning – *"Kan du första normalformen? Inga upprepade grupper i tabellen!"*, *"Vad är skillnaden mellan == och equals()?"*, *"Varför är String immutable?"*, *"Interface eller abstrakt klass? Motivera!"*, *"Checked eller unchecked exception?"* … (tolv frågor, sedan från början). `Battle` skriver ut den som `Lektor Martin: "..."`, och på webben blir den en pratbubbla.
- **Entrédörren är tyngre:** 20 hp och slår 6 (förut 15 och 5). Simulerat 20 000 gånger med svärdet: hjälten vinner dörren **99 %** men har runt **20 hp** kvar i stället för 27. Det märks i Kafferummet, eftersom kaffet kommer först efter JavaFläcken: dörren och Kafferummet klaras tillsammans **56 %** (förut 72 %) (`GameEngineTest.theEntranceDoorIsHeavierNow`).
- **Kafferummet är svårare:** simulerat 20 000 gånger med pilbågen från Skoldörren (riktiga `Battle`): JavaFläcken vinner man **87 %** (förut 99 %), trollet och trollkarlen efter kaffet **82 %** (förut 90 %), hela rummet **72 %** (förut 89 %). Pilen gör 14 mot nivå 2, så hp-gränserna 12 och 24 avgör hur många skott det krävs – trollet faller fortfarande på ett skott, men slår hårdare om det hinner (`RoomTest`, `GameEngineTest.theJavaFleckIsToughNow`).
- Martin har **100 hp** (`Martin.HP`) och slår **15** i fas 2. Stressbollen läker **6** per runda (`WorldBuilder.STRESS_BALL_HEAL`) och Dela skärm **12** (`KnowledgeAttack.SCREENSHARE_HEAL`) – tillsammans 18, lite mer än han slår. Utan slump räcker boken inte (Martin hinner förklara 7 + 7 + 7 + 15 + 15 = 51 > 40), men boken, bollen och Dela skärm vid 15 hp vinner (`MartinTest`).
- **Lavalampan** (`Boss.withLavaLamp`, `Martin.LAVA_LAMP_HEAL`): i fas 2 tar Martin fram en lavalampa – `Lektor Martin tar fram en lavalampa och ställer den på katedern! Den ger honom +3 hp varje runda – krossa den!` med en egen bild (`AttackArt.LAVA_LAMP`). Före varje förklaring bubblar den: `Lavalampan bubblar: Lektor Martin +3 hp` (aldrig över 100). Så länge den står finns **Krossa lavalampan** sist i attackmenyn, **gulmarkerad** (`combat/LavaLampSmash`, `AttackMenu.options(hero, foe)`): ingen skada på Martin och den missar aldrig, men den kostar din tur – sedan bubblar lampan inte mer och Enter citerar ur boken igen (`Hero.stopUsing`). Konsolen har en film (`o==[] KRASCH!`) och ljud (club); på webben står lampan med bubblande lava under Martins mätare i stridspanelen, lyser upp när den ger hp och spricker och faller när den krossas (`LavaLampTest`). Simulerat 20 000 gånger per sätt (stressbollen i fickan, Dela skärm vid 15 hp): krossar man lampan direkt vinner man **76 %**, struntar man i den **46 %**.
- **USB-minnet** (`combat/UsbVirusAttack`, attackmenyn 6 eller 7): smyg in ett USB-minne med virus i Martins dator. **20 % chans**: viruset tar över datorn och hjälten vinner **direkt** – skadan är precis Martins hp, och Battle varierar den inte (`instantWin()`). Annars: `Robert försöker smyga in ett USB-minne, men blir påkommen av Lektor Martin!` och Martin tar det. **Ett försök** – `used(hero)` anropas efter försöket och `Hero.loseUsbStick()` tar bort det ur menyn, och Enter efteråt citerar ur boken. Egen bild (`AttackArt.USB_VIRUS`), eget ljud (zap när det lyckas, buzz när man blir påkommen), en film i konsolen (`[USB]> VIRUS!` / `PÅKOMMEN!`) och på webben: USB-minnet glider in, skärmen glitchar, virus flyger upp och *VIRUS! DATORN ÄR ÖVERTAGEN*. Simulerat: USB-minnet i första rundan, sedan taktiken och lampan krossad vinner **80 %** (`UsbVirusAttackTest`).

### `combat/Character` – vem som slåss
Abstrakt – man skapar en `Hero` eller `Enemy`, aldrig en `Character`. Subklasserna måste skriva:
- `getAttackBonus()` – Hero: från vapnet. Enemy: från nivån.
- `getDefense()` – Hero: från rustningen. Enemy: från nivån.

`Character` **är** ingen `AttackStrategy`, den **har** en (komposition). `attack(target)` frågar strategin om skadan.

### `items/Item` + `Weapon`, `Armor`, `Potion` – föremålen
`sealed ... permits` betyder att exakt de tre recorden får vara ett `Item`; en fjärde stoppas av kompilatorn. Records är ren data – Java skriver konstruktor och getters. Här kopplas B:s föremål in i A:s strid: `Hero.equip(Weapon)` ger `getAttackBonus()`.

### `game/GameEventListener` – vem som får veta vad som händer
Observer-mönstret: A:s `Battle` anropar `onEvent("Hjälten gör 7 i skada")` utan att veta vem som lyssnar. C:s `EventBus` skickar vidare till alla registrerade lyssnare, t.ex. `ConsoleEventLogger` (skärm), `FileLogger` (filen `fangdjupet.log`, en rad per händelse, färgkoderna borttagna) och `sound/SoundListener` (musik och ljud, se nedan).

### `sound/` – musik och ljud
Retromusik i japansk RPG-stil och medvetet fula 8-bitarsljud, **syntade i Java Sound** (`javax.sound.sampled`) – inga ljudfiler. Samma låtar och ljud som webbversionen (`fangdjupet-web`, `sound.js`).
- `SoundListener` är en lyssnare till på `EventBus` (Observer) och väljer ljud efter händelserna. Varje rum har en egen låt: huvudmenyn (yo-skalan), Skoldörren, Kafferummet (ryūkyū), en **klibbig** låt i städskrubben (iwato: tonerna glider in i varandra som i såpa, svajar och är ostämda, med ett "plopp" som en mopp i hinken) och Lektionssalen (in-skalan). `Fiende ... dyker upp!` byter till stridsmusik – mot StädHäxan en snabbare klibbig låt och mot Lektor Martin egen bossmusik. Jinglar vid vinst, game over och när Fångdjupet är vunnet.
- Ljudeffekter: blipp när man trycker Enter (`GameEngine.onInput`), ett skränigt krossat "PJOOO" när pilbågen skjuter, magi, svärdshugg, kunskap mot Martin, miss, skada, kaffe (glugg), föremål, Entrédörren som slår igen och StädHäxans skratt.
- **Varje fiende låter på sitt sätt** (`SoundListener.enemySound`): trollets klubba (dov duns), trollkarlens zap, tvättsvampen (blöt smäll), Svinto som väser, kemikalierna som fräser och bubblar, och Martins *bla-bla-bla*. Java-boken bläddrar, att slinta på sidan låter *wiiiuuu – duns*, Dela skärm klingar, stressbollen piper när den kläms och säger *boing* när den kastas. När man plockar upp: vapnet klingar i handen, rustningen klonkar, kanelbullen mumsas. Har hjälten en fjärdedel hp eller mindre efter ett slag piper en varning.
- **Animation i konsolen** (`game/AnimatedConsoleLogger`, en lyssnare på `EventBus` i stället för `ConsoleEventLogger`): bilderna ritas fram rad för rad och repliker – som Martins frågor – skrivs ut bokstav för bokstav. I striden spelas små filmer upp på en rad (med ``, tillbaka till radens början), sedan suddas de och den vanliga raden skrivs som förut: pilen `>>---->` flyger och säger THWACK!, magin sprakar till *Z A P !*, svärdet svingar `/ | ` till SLASH!, boken skickar `{;}`, stressbollen studsar till SQUISH!, Dela skärm skriver `> java Hello!`, och att slinta låter *~wiiiuuu~ ...duns.* Fienderna smäller med BONK!, ZAP!, SPLAT!, PSSST!, FZZZ! eller BLA BLA BLA!, en miss susar förbi (~ ~ ~ MISS!), hp-mätaren tappar steg för steg i rött (fylls på i grönt), prickarna tickar när du letar, `!!!` när en fiende dyker upp (B O S S för Martin) och stjärnor när du vinner. Ljudet ligger först på `EventBus`, så smällen hörs när filmen börjar. Av med `-Dfangdjupet.animation=av` eller `FANGDJUPET_ANIMATION=av`; webbversionen stänger av den och animerar själv. Testerna fångar utskriften och pauserna, så de väntar aldrig.
- **Tydligare grafik i konsolen** (`game/ConsoleStyle`): hp-mätarna ritas som färgade block (`Robert  █████░░░░░  20/40`, grönt över halva livet, gult, sedan rött), hjälten slår med `»` och fienden med `«`, och skadan står i fet gul (`−14`). Rummets namn blir en rubrik (`▌ KAFFERUMMET`), menyvalen får siffran i fet gul (`1. Strid mot rummets fiende`, utan ruta), kartan färgar `[@]` gult, `[x]` grönt, `[!]` rött, `[?]` cyan och tonar ned `[#]`, och repliker, vinster, fynd och fiender som dyker upp har egna färger. Bilderna ritas i fet stil. Bara utseendet ändras: spelets rader är desamma, så webben (som kör spelet med animationen av och därför utan färger) och loggfilen får vanlig text (`ConsoleStyleTest`).
- **Webbens utseende i konsolen** (#26, `ConsoleStyle`): klarar terminalen 24-bitarsfärg används webbens färger (`ConsoleStyle.tint`), annars de 16 vanliga. `-Dfangdjupet.farger=24` eller `16` bestämmer, och i IntelliJs Kör-fönster måste man sätta `24` själv. Menyerna och kartan ligger i ramar (`panel`), utrustningen är en panel med ikoner, och ovanför kartan står en statusrad med rum, hp och attack. Raden skrivs före varje meny och ligger inte fast högst upp i fönstret. I striden står hjälten och fienden sida vid sida (`ConsoleStyle.battle`, `Robert  ███████░░░  31/40   vs   JavaFläcken  ████████░░  20/24`). Filmerna har fått blink, skakning, skadesiffror, svep mellan rummen, K.O. och grön glöd. Ikonerna är tecken med en kolumns bredd, inte emojis, som blir olika breda i olika terminaler. Webben kör med `-Dfangdjupet.animation=av` och får samma text som förut.
- **Ljud utanför striden** (`SoundListener.otherSound`): fotsteg när du går till ett rum, prassel när du letar, ett surt *buu-buu* när du inte hittar något, diskett-bip när äventyret sparas, ett *brrrt* vid fel val, glitter när Frida dyker upp, en liten fanfar när äventyret börjar, klick när du byter attack, ett skramlande handtag vid låsta rum, en duns när du låter något ligga kvar, blixtlås när du öppnar utrustningen, pratbubblor för repliker (Martin säger bla-bla) och tre toner nedåt när du avslutar. Magi och avstånd skriver båda "skjuter", så ljudet väljs efter hjältens attack (`GameEngine.heroAttack`).
- `Song` håller låtarna som text: varje token är ett åttondelssteg i en pentatonisk skala (`0` grundtonen, `5` oktaven över, `_` håll, `.` paus) och trummorna `k s h p`.
- `RetroSpeaker` (en `Speaker`) räknar fram ljudet i en egen tråd: fyrkantsvåg, sågtand, triangel, sinus och brus med glidning, vibrato, filter och ett envelopp. Finns inget ljudkort blir det tyst och spelet fungerar som vanligt. **Stäng av** med `-Dfangdjupet.ljud=av` eller `FANGDJUPET_LJUD=av` – webbservern startar spelet så, eftersom webbläsaren spelar sitt eget ljud.
- Testerna (`SoundListenerTest`, `SongTest`) använder en `Speaker` som bara antecknar vad som skulle spelas, och `RetroSpeaker.offline()` som räknar fram ljudet utan högtalare: varje låt och effekt ska höras men inte klippa.

### `game/GameState` + `GameSaveFileHandler` – spara och ladda
`GameState` är en record med hjältens namn, maxHp, hp, rummets nummer (`nextRoom`, 0 = Skoldörren) och ryggsäcken (`items`). Konstruktorn kastar `IllegalArgumentException` om något är orimligt (tomt namn, maxHp ≤ 0, hp utanför 0–maxHp, negativt rum). `GameSaveFileHandler(sparmapp)` har `save(state, slot)`, `load(slot)` och `listSaveSlots()`. Sparplatser är 1 och uppåt. En saknad sparplats, ett saknat värde, fel version eller trasiga data ger `SaveFileException`. Den ärver `IOException`, så kod som fångar `IOException` fångar den också. `save` skriver först till en temporär fil, så att den gamla sparningen finns kvar om något går fel mitt i. `GameEngine` använder sparplats 1 i mappen `saves`: spelmenyn 4 sparar och huvudmenyn 2 läser in (se *Rum, vapen och grafik i spelet*).

### Varför kontraktet finns
Alla kan bygga parallellt från dag 1: B skriver `new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1)` och C skickar händelser innan A:s kod är klar. Bryter någon mot kontraktet kompilerar inte koden – felen syns direkt, inte när allt kopplas ihop. Ändras kontraktet senare sker det via en egen liten PR som alla granskar.

## Kopplingspunkter

Där modulerna möts. **Bestämt** = står i kontraktet och ändras bara via en egen PR. **Förslag** = bestäms i PR:en där koden skrivs – skriv beslutet i PR-beskrivningen och uppdatera tabellen.

| Koppling | Vem | Bestämt i kontraktet | Förslag – bestäms i |
|---|---|---|---|
| Utrustning påverkar striden | A ↔ B | `getAttackBonus()` och `getDefense()` i `Character`; `Weapon.damageBonus()`, `Armor.defense()` | `hero.equip(Weapon)` / `hero.equip(Armor)` – A:s PR 1. Vapnets räckvidd väljer attacken: `RANGED` → `RangedAttack`, `MELEE` → `MeleeAttack`, `BOTH` behåller den gamla – #12 |
| En dryck läker | A ↔ B | `Character.heal(int)`, `Potion.healAmount()` | `hero.drink(Potion)` anropar `heal(potion.healAmount())`, aldrig över max – A:s PR 1 |
| Fiender skapas | B ↔ A | `Character(name, maxHp, attack)` är `protected` | `Enemy(name, maxHp, attack, level)` – A:s PR 1. Vanliga fiender skapas med `new Enemy(...)` i `WorldBuilder` och `GameEngine`, bossarna med fabrikerna `Martin.create()` och `CleaningWitch.create(random)` (#18). Den planerade `EnemyFactory` (B:s PR 3) behövdes inte – Factory finns redan i `ItemFactory` och bossfabrikerna |
| Vad en strid returnerar | A ↔ C | `attack(target)` returnerar skadan, drar inte av hp | `Battle.fight(hero, enemy)` returnerar vinnaren (`Character`) – A:s PR 3 |
| Händelser når loggen | A ↔ C | `GameEventListener.onEvent(String)` | `Battle` får en `GameEventListener` i konstruktorn; C:s `EventBus` skickas in – A:s PR 3. `Battle` loggar varje slag, hp-mätare efter varje träff och ett slutmeddelande (se avsnittet om `Battle`) |
| Inventariet sparas | B ↔ C | alla föremål har `type()`, `description()` och `level()` | sparas sedan #26 som en rad per föremål, t.ex. `item.1=weapon;BOW;RANGED;4;1;Pilbåge` (sort, typ, räckvidd, styrka, nivå och namn). `GameSaveFileHandler` skriver och läser raderna, och bonusen sparas eftersom den slumpas |
| ASCII-grafik i konsolen | A ↔ C | `GameEventListener.onEvent(String)` – spelets händelser, även grafiken, går via lyssnaren och inte direkt till `System.out` (menyerna, kartan och frågorna skrivs med `IO.println`) | Streck och tecken i ren ASCII (`#` `-` `\|` `/`), texterna med å, ä och ö (FÅNGDJUPET, SKOLDÖRREN, PILBÅGE …), ingen JavaFX/Swing och inga nya beroenden. A:s `HpBar.render(character, width)` ger `[#####-----]` för 20/40 hp och `HpBar.status(...)` hela raden `Troll  [#######---] 28/40` för `Battle`-loggen, som använder den sedan PR 3. C:s `AsciiArt` har `TITLE`, `MARTIN`, `FRIDA`, `banner(text)` och `show(listener, art)`. Mergat i #10. Rumskartan kom i #22 (`GameEngine.roomMap`). Sedan #23 ritar `ConsoleStyle` hp-mätarna som färgade block i en riktig konsol; loggfilen och webben får vanlig text |
| Sparfilen | A, B, C | – | #15 valde en `.properties`-fil per sparplats, `save-<nr>.properties`, med nycklarna `version`, `heroName`, `heroMaxHp`, `heroHp` och `nextRoom` (rummets nummer) i stället för `;`-raderna i det första förslaget. `GameEngine` sparar och läser in plats 1 sedan #20. Sedan #26 sparas ryggsäcken också, i de valfria nycklarna `item.0`, `item.1` ..., så gamla sparfiler går att läsa |
