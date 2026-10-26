# grupp8-fangdjupet

## Kom igång

### 1. Installera
- **JDK 25 eller nyare** (krävs av `pom.xml`). Enklast via IntelliJ: *File → Project Structure → SDK → Download JDK*.
- **Maven** behövs bara om du vill köra `mvn` i terminalen – IntelliJ har Maven inbyggt.

### 2. Klona och öppna
- IntelliJ: *File → New → Project from Version Control* → klistra in `https://github.com/ithsjava26/grupp8-fangdjupet` → *Clone*.
- IntelliJ känner igen `pom.xml` och laddar projektet som Maven-projekt. Klicka *Load Maven project* om den frågar.

### 3. Kontrollera att allt fungerar
- Kör `Main` (gröna play-knappen) – den ska skriva ut "Fångdjupet".
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

1. Skapa en pull request på GitHub.
2. Någon annan i gruppen granskar och godkänner innan merge.
3. Efter merge: kör `git checkout main` och `git pull` för att få senaste koden.
