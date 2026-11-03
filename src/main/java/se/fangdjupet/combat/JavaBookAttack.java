package se.fangdjupet.combat;

import java.util.List;

// Java-boken som hjälten hittar på katedern i Lektionssalen. Varje gång hjälten citerar ur den
// bläddrar hen vidare: kapitel 18 (Polymorphism), sedan kapitel 20 (Interface), sedan från början igen.
// 5 i skada, rakt igenom försvaret som all kunskap, och den kostar ingenting
// (StackOverflow gör 6 men kostar 1 hp). Varje hjälte har sin egen bok (Hero.pickUpJavaBook).
public class JavaBookAttack implements AttackStrategy {
    public static final int POWER = 5;
    public static final List<String> CHAPTERS = List.of("kapitel 18: Polymorphism", "kapitel 20: Interface");

    private int next;          // kapitlet hjälten läser nästa gång
    private String lastRead;   // kapitlet hen just läste – syns i loggen

    // Kapitlet som står på tur, t.ex. "kapitel 20: Interface"
    public String nextChapter() {
        return CHAPTERS.get(next);
    }

    // Läser kapitlet och bläddrar vidare till nästa
    @Override
    public int calculateDamage(Character attacker, Character target) {
        lastRead = nextChapter();
        next = (next + 1) % CHAPTERS.size();
        return POWER;
    }

    // I attackmenyn: kapitlet som står på tur
    @Override
    public String name() {
        return "Citera från Java-boken, " + nextChapter();
    }

    // I loggen: "Robert citerar kapitel 18: Polymorphism för Kapten Martin skada: 5"
    @Override
    public String verb() {
        return "citerar " + (lastRead == null ? nextChapter() : lastRead) + " för";
    }
}
