package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AttackArtTest {

    // Sant om alla tecken är vanlig ASCII (0–127) eller svenska å, ä, ö och é (som resten av spelets text)
    private static boolean isPlainAscii(String s) {
        return s.chars().allMatch(ch -> ch < 128 || "åäöÅÄÖéÉ".indexOf(ch) >= 0);
    }

    // Alla attacker som finns i spelet: hjältens, Martins två faser och StädHäxans tre kast
    private static List<AttackStrategy> allAttacks() {
        Boss martin = Martin.create();
        AttackStrategy phaseOne = martin.getAttack();
        martin.takeDamage(martin.getMaxHp() / 2);                  // halva livet: fas 2
        AttackStrategy phaseTwo = martin.getAttack();
        return List.of(
                new MeleeAttack(10), new MagicAttack(6), new RangedAttack(5, new Random()),
                KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE, new JavaBookAttack(),
                phaseOne, phaseTwo,
                CleaningAttack.SPONGE, CleaningAttack.SVINTO, CleaningAttack.CHEMICALS);
    }

    @Test
    void everyAttackInTheGameHasArt() {
        for (AttackStrategy attack : allAttacks()) {
            assertTrue(AttackArt.forAttack(attack).isPresent(), "saknar bild: " + attack.name());
        }
    }

    @Test
    void artIsPlainAsciiAndFitsTheConsole() {
        for (AttackStrategy attack : allAttacks()) {
            String art = AttackArt.forAttack(attack).orElseThrow();
            assertTrue(isPlainAscii(art), "ASCII: " + attack.name());
            assertTrue(art.lines().allMatch(line -> line.length() <= 60), "för bred: " + attack.name());
        }
    }

    @Test
    void eachAttackHasItsOwnPicture() {
        long distinct = allAttacks().stream().map(a -> AttackArt.forAttack(a).orElseThrow()).distinct().count();
        assertEquals(allAttacks().size(), distinct);
    }

    @Test
    void doomscrollShowsThePhone() {
        assertEquals(AttackArt.DOOMSCROLL, AttackArt.forAttack(KnowledgeAttack.DOOMSCROLL).orElseThrow());
        assertTrue(AttackArt.DOOMSCROLL.contains("DOOMSCROLLA"));
    }

    @Test
    void martinsTwoPhasesHaveDifferentPictures() {
        Boss martin = Martin.create();
        assertEquals(AttackArt.MARTIN_EXPLAINS, AttackArt.forAttack(martin.getAttack()).orElseThrow());
        martin.takeDamage(martin.getMaxHp() / 2);
        assertEquals(AttackArt.MARTIN_UTTERLY, AttackArt.forAttack(martin.getAttack()).orElseThrow());
    }

    @Test
    void cleaningAttackIsFoundByNameNotAsAnArrow() {
        // CleaningAttack ärver RangedAttack, men ska ha StädHäxans bild – inte pilen
        assertEquals(AttackArt.SVINTO, AttackArt.forAttack(CleaningAttack.SVINTO).orElseThrow());
        assertNotEquals(AttackArt.RANGED, AttackArt.forAttack(CleaningAttack.SPONGE).orElseThrow());
    }

    @Test
    void basicAttacksAreFoundByType() {
        assertEquals(AttackArt.MELEE, AttackArt.forAttack(new MeleeAttack(3)).orElseThrow());
        assertEquals(AttackArt.MAGIC, AttackArt.forAttack(new MagicAttack(3)).orElseThrow());
        assertEquals(AttackArt.RANGED, AttackArt.forAttack(new RangedAttack(3, new Random(1))).orElseThrow());
    }

    @Test
    void unknownAttackAndNullHaveNoArt() {
        assertTrue(AttackArt.forAttack((a, t) -> 1).isEmpty());   // en lambda har inget namn med bild
        assertTrue(AttackArt.forAttack(null).isEmpty());
    }
}
