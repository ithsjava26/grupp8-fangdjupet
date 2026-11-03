# grupp8-fangdjupet

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
 └─ KnowledgeAttack A, PR 3 (Dela skärm: 50/50)
        ▲
        └──── Character HAR en AttackStrategy (fältet `attack`)

AttackChooser (interface)   A – spelaren väljer attack per runda
Frida (ingen Character)     A – ger två drycker före Martin

Item (sealed interface)             GameEventListener (interface)
 ├─ Weapon   record                  ├─ EventBus            C, PR 1
 ├─ Armor    record                  ├─ ConsoleEventLogger  C, PR 1
 └─ Potion   record                  └─ FileLogger          C – fangdjupet.log
```

### `combat/AttackStrategy` – hur man slåss
En metod, `calculateDamage(attacker, target)`, som varje strategi räknar på sitt sätt:
- `MeleeAttack` – styrka + anfallarens bonus − målets försvar.
- `MagicAttack` – fast kraft, rustningen påverkar inte.
- `RangedAttack` – slumpar med `Random` (20 % miss), annars lägre skada.
- `KnowledgeAttack` – fast kraft som ignorerar försvaret, som `MagicAttack`, men vissa attacker kostar anfallaren hp (`DOOMSCROLL`, `STACKOVERFLOW`). `SCREENSHARE` (Dela skärm) är en chansning: 50/50 mellan 1 och 6 i skada (konstruktorn `KnowledgeAttack(namn, låg, hög, Random)`).
- `CleaningAttack extends RangedAttack` – StädHäxans kast. Samma uträkning som pilbågen (20 % miss, annars styrka + halva bonusen − försvaret), men med eget namn och verb: `SPONGE` *kastar en tvättsvamp på* (6), `SVINTO` *kastar Svinto på* (8) och `CHEMICALS` *sprayar kemikalier på* (10).
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
- `GameEngine` kör striden med `Battle` (spelmenyn 1) och frågar spelaren vilken attack som ska användas varje runda (Enter behåller den nuvarande). Menyn kommer från `game/AttackMenu`: `options(hero)` ger närstrid, magi och avstånd – eller bara Martins fyra `KnowledgeAttack` när hjälten har Java-boken. `lines(...)` ger menyraderna och `choose(options, inmatning)` gör om det spelaren skrev till en attack (`null` = behåll). Den går att testa utan tangentbord.
- **Två menyer** (`GameEngine.menu(room)`): utan äventyr visas **huvudmenyn** (1. Starta nytt äventyr, 2. Fortsätt sparat äventyr, e. Avsluta). I spelet visas **spelmenyn** med rummets namn som rubrik (1. Strid mot rummets fiende, 2. Gå till ett annat rum, 3. Visa hjältens utrustning, 4. Spara äventyret, 5. Leta i rummet, e. Avsluta). Efter game over och efter vinsten kommer huvudmenyn tillbaka.
- **Inte förbi en levande fiende:** spelmenyn 2 går bara vidare när rummet är rensat. Annars skriver spelet `Entrédörren står i vägen! Besegra rummets fiender innan du går vidare.` (`GameEngine.blockedBy(room)`). Det gäller också efter rundtaket, där hjälten kan vinna på mest hp fast fienden lever.
- **Visa hjältens utrustning** (spelmenyn 3, `GameEngine.equipment(hero)`): hp-mätaren, vapnet, rustningen, attacken och Java-boken – och sedan bilder på vapnet och rustningen hjälten bär. Till exempel:
  ```
  Robert  [########--] 34/40
  Vapen: Pilbåge (+3 i skada)
  Rustning: ingen
  Attack: Närstrid
  ```
- **Leta i rummet** (spelmenyn 5, `Room.withHiddenItem` / `Room.search()`): gömda föremål hittas bara om man letar, och bara en gång. I **städskrubben** (Korridoren och städskrubben) ligger **Städrustningen** – gummiförkläde, hink som hjälm och moppen – med **+20 i försvar** (`Du tar på dig Städrustning: +20 i försvar.`). Med den gör närstrid och StädHäxans kast nästan ingen skada, men Martins kunskap går rakt igenom rustningen – och **Martin blir argare** när han ser den (`Martin.enrage`, `Boss.enrage`): `Kapten Martin ser Städrustningen och blir arg! Han förklarar hårdare.` och repliken *"Jaså, städrustning? Kunskap går rakt igenom gummi!"*. Hans första förklaring gör då 4 i skada i stället för 3 (fas 2 är kvar på 5). Vid fullt hp vinner hjälten ungefär 78 % i stället för 89 %. I de andra rummen: `Du hittar inget här.` Rustningen sparas inte (inventariet sparas inte än). `ItemType` har ingen egen rustningssort, så rustningen är en `SHIELD`.
- **Bilder för föremålen:** `AsciiArt.forItem(item)` tar först bilden på namnet (Pilbåge, Trollstav, kaffet, Städrustningen) och annars på sorten: svärd, spjut, båge, stav, sköld, hjälm och dryck. Den visas när man plockar upp eller hittar något och under Visa hjältens utrustning.
- **Spara och fortsätta:** spelmenyn 4 sparar hjältens namn, maxHp, hp och rummets nummer på sparplats 1 i mappen `saves` (`Äventyret är sparat: Kafferummet, 34/40 hp.`). En gammal sparning skrivs över. Huvudmenyn 2 läser in den: `Välkommen tillbaka, Robert!`, och hjälten står **i början av det sparade rummet** med samma hp. Rummen före är avklarade och fienderna där kommer inte tillbaka, men vapnen som låg där tar hjälten med sig (`GameEngine.fastForward`). Finns ingen sparning skriver spelet `Det finns inget sparat äventyr. Välj 1 för att starta ett nytt.` En trasig sparfil ger `Sparfilen går inte att läsa: ...` och spelet fortsätter. Inventariet sparas inte, och vapnens bonus slumpas på nytt (`ItemFactory.createWeapon`).
- **Loggfil:** `Main` registrerar en `FileLogger` på `EventBus`, så allt som händer också skrivs till `fangdjupet.log`. Färgkoderna runt GAME OVER tas bort i filen. Går filen inte att skriva fortsätter spelet ändå. `saves/` och `fangdjupet.log` ligger i `.gitignore`.
- **Föremål plockas upp automatiskt** när hjälten kommer in i rummet (`GameEngine.pickUpAll`), som Java-boken. Ett vapen hamnar i handen och ger sin bonus i skada (`Du har nu Pilbåge i handen: +4 i skada.`), men attacken byts inte: pilbågen (avstånd 5) är svagare än närstrid (10), och den som trycker Enter ska inte få sämre attack. Avstånd och magi väljs i attackmenyn. 1000 simulerade spel: 818 vinster med vapnen i handen, 54 om pilbågen hade bytt attack.
- `AsciiArt.forRoom(rumsnamn)` ger en ASCII-bild för Skoldörren, Kafferummet (kaffeautomaten med JAVA-skylten), Korridoren och städskrubben och Lektionssalen (tom `Optional` för okända rum). `showCurrentRoom()` visar den, och dessutom det hjälten plockar upp (`Du hittar och plockar upp: Pilbåge` med bild).
- `AsciiArt.forName(namn)` ger en bild för fiender, saker och personer: Entrédörren ("Har du passerkort?"), JavaFläcken, Troll, Trollkarl, StädHäxan, Kapten Martin, Frida, kaffekoppen, kanelbullen, Java-boken, pilbågen och trollstaven. Den visas när fienden dyker upp, när man tar kaffet eller kanelbullen, när Frida kommer och när man plockar upp något.
- **Bilder för attackerna:** `combat/AttackArt.forAttack(attack)` ger en bild för varje attack i spelet – närstrid (svärd), magi, avstånd (pil), DoomScrolla (mobilen som scrollar), JavaQuizet, StackOverflow, Dela skärm, Martins två förklaringar och StädHäxans tvättsvamp, Svinto och kemikalier. Bilden hittas först på attackens namn, sedan på typen – `CleaningAttack` ärver `RangedAttack` men får StädHäxans bild, inte pilen. `Battle` visar bilden när spelaren väljer en attack (Enter = behåll visar ingen), och första gången en boss använder en attack i striden. Det slås på med `new Battle(...).withAttackArt()` – av som standard, så att stridstesterna kan räkna loggraderna.
- **Vinst:** när Martin är besegrad visar `GameEngine` `AsciiArt.VICTORY` (en pokal och *D U   V A N N !*) före `Robert vinner Fångdjupet!`.
- **Flera fiender i ett rum:** `Room` kan ta en lista med fiender. `getEnemy()` ger den första som lever och `null` när rummet är rensat. Vinner hjälten och rummet har fler fiender skriver spelet `Men vänta... Troll har gömt sig i Kafferummet och hoppar fram!`, och nästa strid (menyval 1) är mot den.
- **Flera fiender samtidigt:** `room.thenTogether(troll, wizard)` lägger till en våg där fienderna slåss tillsammans. `room.getEnemies()` ger den aktuella vågen och `Battle.fight(hero, List<Enemy>)` kör striden. Varje runda slår hjälten den första fienden som lever, och sedan slår varje levande fiende i tur och ordning. Slutraden blir `Robert vinner mot Troll och Trollkarl!`.
- **Paus mellan vågorna:** `room.withRefreshment(replik, dryck)` ger något att dricka när första vågen är besegrad, bara en gång. `GameEngine.nextWaveIsReady` kollar att hela vågen verkligen är död – vid rundtaket kan hjälten vinna på mest hp fast fiender lever, och då kommer varken kaffet eller nästa våg. I Kafferummet: `Du ser dig om efter en kaffe... Där! Kannan på bänken är fortfarande varm.` och `Robert tar en kopp kaffe (+11 hp)`. Kaffet ger 30, men siffran är det hjälten faktiskt fick (aldrig över max).
- **Belöning när rummet är rensat:** `room.withReward(replik, dryck)`, bara en gång. I Kafferummet: `På bänken bredvid kannan ligger en kanelbulle. Den har ingen tagit!` och `Robert tar en kanelbulle (+27 hp)`. Kanelbullen ger 35.
- **Repliker:** `new Enemy(...).withLine("...")` ger fienden något att säga. Repliken skrivs ut när fienden dyker upp, t.ex. `Trollkarl: "Thou shall not pass!"`.
- **Ingen "Prata med någon" i menyn:** karaktärerna pratar av sig själva när de dyker upp – fienderna med sina repliker och Frida när StädHäxan är besegrad. Därför finns inget sådant menyval.
- **Game over:** dör hjälten skriver `GameEngine` GAME OVER i stora röda bokstäver (`AsciiArt.GAME_OVER` i `AsciiArt.red(...)`, ANSI-färgkoden `ESC[31m`), vem som vann – eller att hjälten dog av sin egen attack – och att 1 i huvudmenyn startar om. Äventyret nollställs och huvudmenyn kommer tillbaka. Färgen syns i IntelliJ och Windows Terminal; bilden i sig är ren ASCII.

#### Rummen och fienderna

| Rum | Fiender (i tur och ordning) | Föremål |
|---|---|---|
| Skoldörren | Entrédörren – liten fiende, 15 hp, närstrid 5 | – |
| Kafferummet | JavaFläcken – liten fiende, 15 hp, närstrid 4. Sedan en **kaffe** från kaffeautomaten (+30 hp, aldrig över max). Sist de riktiga fienderna som gömt sig där, **samtidigt**: **Troll** (12 hp, närstrid 7, nivå 2) och **Trollkarl** (16 hp, magi 6, nivå 2, säger *"Thou shall not pass!"*). När rummet är rensat: en **kanelbulle** (+35 hp). | Pilbåge |
| Korridoren och städskrubben | **StädHäxan** (`CleaningWitch.create(random)`, en `Boss`) – 30 hp, nivå 2. Kastar tvättsvamp eller Svinto (slumpas varje slag), och vid halva livet sprayar hon kemikalier. Alla tre är avståndsattacker. Hotar: *"Ett steg till mot lektionssalen och jag tar fram kemikalierna!"* När hon är besegrad dyker **Frida** upp (se nedan). | Trollstav (staff) |
| Lektionssalen | **Kapten Martin** (`Martin.create()`, en `Boss`) – 40 hp, nivå 3. Förklarar 3 per slag, vid halva livet 5. Den sista bossen: besegrad = `vinner Fångdjupet!` | Java-boken (tas upp direkt) |

#### Hjältens hp genom spelet (körning utan slump i `Battle`, närstrid och StackOverflow mot Martin)

| Efter | Robert |
|---|---|
| Entrédörren | 34/40 |
| JavaFläcken | 29/40 |
| Kaffe | 40/40 |
| Troll och Trollkarl | 13/40 |
| Kanelbulle | 40/40 |
| StädHäxan | 9–11/40 om inget av hennes kast missar (i en riktig körning 18/40) |
| Fridas två drycker | 40/40 – full hälsa mot Martin |
| Kapten Martin | 9/40 – **vinner Fångdjupet!** |

#### Svårighet med slumpen
1000 simulerade spel med samma val (närstrid med vapnen i handen, sedan StackOverflow mot Martin): **795 vinster**. Förlusterna: 51 mot trollet och trollkarlen, 149 mot Martin (37 av dem av StackOverflows egen kostnad) och 5 mot StädHäxan. Om man bara delar skärm mot Martin vinner man 357 av 1000. Siffrorna varierar några procent mellan körningar.

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
- `CleaningWitch.create(random)` ger StädHäxan som en `Boss`: fas 1 är en `RandomAttack` av `SPONGE` och `SVINTO`, och fas 2 (vid 15 hp) är `CHEMICALS`.
- `Martin.create()` ger en `Boss`: Kapten Martin, 40 hp, nivå 3. Han förklarar med `KnowledgeAttack`: *Thoroughly explained the Hello World Java to you* (3), och under halva livet *Utterly explained* (5).
- `Martin.heroChoices()` ger hjältens fyra val mot honom: `DOOMSCROLL`, `QUIZ`, `STACKOVERFLOW` och `SCREENSHARE`.
- **Java-boken:** Lektionssalen har den (`room.withJavaBook()`, `takeJavaBook()` bara en gång). När man kommer in tar hjälten upp den med `hero.pickUpJavaBook()`, som byter attack till StackOverflow. `hero.hasJavaBook()` gör att `AttackMenu` bara visar de fyra.
- Martins porträtt (`AsciiArt.MARTIN`) och repliken *"Let me explain Hello World to you..."* visas när han dyker upp.
- Med full hp och StackOverflow går det att vinna. Går Dela skärm dåligt varje gång vinner Martin. `MartinTest` kollar båda.

### `combat/Character` – vem som slåss
Abstrakt – man skapar en `Hero` eller `Enemy`, aldrig en `Character`. Subklasserna måste skriva:
- `getAttackBonus()` – Hero: från vapnet. Enemy: från nivån.
- `getDefense()` – Hero: från rustningen. Enemy: från nivån.

`Character` **är** ingen `AttackStrategy`, den **har** en (komposition). `attack(target)` frågar strategin om skadan.

### `items/Item` + `Weapon`, `Armor`, `Potion` – föremålen
`sealed ... permits` betyder att exakt de tre recorden får vara ett `Item`; en fjärde stoppas av kompilatorn. Records är ren data – Java skriver konstruktor och getters. Här kopplas B:s föremål in i A:s strid: `Hero.equip(Weapon)` ger `getAttackBonus()`.

### `game/GameEventListener` – vem som får veta vad som händer
Observer-mönstret: A:s `Battle` anropar `onEvent("Hjälten gör 7 i skada")` utan att veta vem som lyssnar. C:s `EventBus` skickar vidare till alla registrerade lyssnare, t.ex. `ConsoleEventLogger` (skärm) och `FileLogger` (filen `fangdjupet.log`, en rad per händelse, färgkoderna borttagna).

### `game/GameState` + `GameSaveFileHandler` – spara och ladda
`GameState` är en record med hjältens namn, maxHp, hp och rummets nummer (`nextRoom`, 0 = Skoldörren). Konstruktorn kastar `IllegalArgumentException` om något är orimligt (tomt namn, maxHp ≤ 0, hp utanför 0–maxHp, negativt rum). `GameSaveFileHandler(sparmapp)` har `save(state, slot)`, `load(slot)` och `listSaveSlots()`. Sparplatser är 1 och uppåt. En saknad sparplats, ett saknat värde, fel version eller trasiga data ger `SaveFileException`. Den ärver `IOException`, så kod som fångar `IOException` fångar den också. `save` skriver först till en temporär fil, så att den gamla sparningen finns kvar om något går fel mitt i. `GameEngine` använder sparplats 1 i mappen `saves`: spelmenyn 4 sparar och huvudmenyn 2 läser in (se *Rum, vapen och grafik i spelet*).

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
| Händelser når loggen | A ↔ C | `GameEventListener.onEvent(String)` | `Battle` får en `GameEventListener` i konstruktorn; C:s `EventBus` skickas in – A:s PR 3. `Battle` loggar varje slag, hp-mätare efter varje träff och ett slutmeddelande (se avsnittet om `Battle`) |
| Inventariet sparas | B ↔ C | alla föremål har `type()`, `description()` och `level()` | sparas som `sword;3`; B lägger till en metod som läser tillbaka raden (gamla `ItemFactory.create("sword", 3)` togs bort i #8) – B:s nästa PR och C:s PR 2 |
| ASCII-grafik i konsolen | A ↔ C | `GameEventListener.onEvent(String)` – all utskrift, även grafiken, går via lyssnaren och aldrig direkt till `System.out` | Bara ren ASCII (`#` `-` `\|` `/`), ingen JavaFX/Swing och inga nya beroenden. A:s `HpBar.render(character, width)` ger `[#####-----]` för 20/40 hp och `HpBar.status(...)` hela raden `Troll  [#######---] 28/40` för `Battle`-loggen, som använder den sedan PR 3. C:s `AsciiArt` har `TITLE`, `MARTIN`, `FRIDA`, `banner(text)` och `show(listener, art)`. Mergat i #10; rumskartan kommer med C:s `World` |
| Sparfilen | A, B, C | – | #15 valde en `.properties`-fil per sparplats, `save-<nr>.properties`, med nycklarna `version`, `heroName`, `heroMaxHp`, `heroHp` och `nextRoom` (rummets nummer) i stället för `;`-raderna i det första förslaget. `GameEngine` sparar och läser in plats 1 sedan `feature/spara-och-fortsatt`. Inventariet sparas inte än – B:s `Inventory` behövs först |
