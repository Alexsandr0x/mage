/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 * 
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 * 
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 * 
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.b;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.condition.common.IsStepCondition;
import mage.abilities.costs.common.TapSourceCost;
import mage.abilities.decorator.ConditionalActivatedAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.RemoveFromCombatTargetEffect;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.SubType;
import mage.constants.Outcome;
import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.filter.common.FilterAttackingCreature;
import mage.filter.common.FilterBlockingCreature;
import mage.filter.predicate.Predicate;
import mage.game.Game;
import mage.game.combat.CombatGroup;
import mage.game.events.GameEvent;
import mage.game.permanent.Permanent;
import mage.players.Player;
import mage.target.TargetPermanent;
import mage.target.common.TargetAttackingCreature;
import mage.watchers.common.BlockedByOnlyOneCreatureThisCombatWatcher;

/**
 *
 * @author L_J
 */
public class BalduvianWarlord extends CardImpl {

    public BalduvianWarlord(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}");
        this.subtype.add(SubType.HUMAN);
        this.subtype.add(SubType.BARBARIAN);
        this.power = new MageInt(3);
        this.toughness = new MageInt(2);

        // {T}: Remove target blocking creature from combat. Creatures it was blocking that hadn't become blocked by another creature this combat become unblocked, then it blocks an attacking creature of your choice. Activate this ability only during the declare blockers step.
        Ability ability = new ConditionalActivatedAbility(Zone.BATTLEFIELD, new BalduvianWarlordUnblockEffect(), new TapSourceCost(), new IsStepCondition(PhaseStep.DECLARE_BLOCKERS, false));
        ability.addTarget(new TargetPermanent(new FilterBlockingCreature()));
        this.addAbility(ability, new BlockedByOnlyOneCreatureThisCombatWatcher());
    }

    public BalduvianWarlord(final BalduvianWarlord card) {
        super(card);
    }

    @Override
    public BalduvianWarlord copy() {
        return new BalduvianWarlord(this);
    }

}

class BalduvianWarlordUnblockEffect extends OneShotEffect {

    public BalduvianWarlordUnblockEffect() {
        super(Outcome.Benefit);
        this.staticText = " Remove target blocking creature from combat. Creatures it was blocking that hadn't become blocked by another creature this combat become unblocked, then it blocks an attacking creature of your choice";
    }

    public BalduvianWarlordUnblockEffect(final BalduvianWarlordUnblockEffect effect) {
        super(effect);
    }

    @Override
    public BalduvianWarlordUnblockEffect copy() {
        return new BalduvianWarlordUnblockEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Player controller = game.getPlayer(source.getControllerId());
        Permanent permanent = game.getPermanent(source.getTargets().getFirstTarget());
        if (controller != null && permanent != null) {

            // Remove target creature from combat
            Effect effect = new RemoveFromCombatTargetEffect();
            effect.apply(game, source);

            // Make blocked creatures unblocked
            BlockedByOnlyOneCreatureThisCombatWatcher watcher = (BlockedByOnlyOneCreatureThisCombatWatcher) game.getState().getWatchers().get(BlockedByOnlyOneCreatureThisCombatWatcher.class.getSimpleName());
            if (watcher != null) {
                Set<CombatGroup> combatGroups = watcher.getBlockedOnlyByCreature(permanent.getId());
                if (combatGroups != null) {
                    for (CombatGroup combatGroup : combatGroups) {
                        if (combatGroup != null) {
                            combatGroup.setBlocked(false);
                        }
                    }
                }
            }

            // Choose new creature to block
            if (permanent.isCreature()) {
                // according to the following mail response from MTG Rules Management about False Orders:
                // "if Player A attacks Players B and C, Player B's creatures cannot block creatures attacking Player C"
                // therefore we need to single out creatures attacking the target blocker's controller (disappointing, I know)
                
                List<Permanent> list = new ArrayList<>();
                for (CombatGroup combatGroup : game.getCombat().getGroups()) {
                    if (combatGroup.getDefendingPlayerId().equals(permanent.getControllerId())) {
                        for (UUID attackingCreatureId : combatGroup.getAttackers()) {
                            Permanent targetsControllerAttacker = game.getPermanent(attackingCreatureId);
                            list.add(targetsControllerAttacker);
                        }
                    }
                }
                Player targetsController = game.getPlayer(permanent.getControllerId());
                if (targetsController != null) {
                    FilterAttackingCreature filter = new FilterAttackingCreature("creature attacking " + targetsController.getLogName());
                    filter.add(new PermanentInListPredicate(list));
                    TargetAttackingCreature target = new TargetAttackingCreature(1, 1, filter, true);
                    if (target.canChoose(source.getSourceId(), controller.getId(), game)) {
                        while (!target.isChosen() && target.canChoose(controller.getId(), game) && controller.canRespond()) {
                            controller.chooseTarget(outcome, target, source, game);
                        }
                    } else {
                        return true;
                    }
                    Permanent chosenPermanent = game.getPermanent(target.getFirstTarget());
                    if (chosenPermanent != null && permanent != null && chosenPermanent.isCreature() && controller != null) {
                        CombatGroup chosenGroup = game.getCombat().findGroup(chosenPermanent.getId());
                        if (chosenGroup != null) {
                            // Relevant ruling for Balduvian Warlord:
                            // 7/15/2006 	If an attacking creature has an ability that triggers “When this creature becomes blocked,” 
                            // it triggers when a creature blocks it due to the Warlord’s ability only if it was unblocked at that point.
                            
                            boolean notYetBlocked = chosenGroup.getBlockers().isEmpty();
                            chosenGroup.addBlocker(permanent.getId(), controller.getId(), game);
                            if (notYetBlocked) {
                                game.fireEvent(GameEvent.getEvent(GameEvent.EventType.CREATURE_BLOCKED, chosenPermanent.getId(), null));
                            }
                            game.fireEvent(GameEvent.getEvent(GameEvent.EventType.BLOCKER_DECLARED, chosenPermanent.getId(), permanent.getId(), permanent.getControllerId()));
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }
}

class PermanentInListPredicate implements Predicate<Permanent> {

    private final List<Permanent> permanents;

    public PermanentInListPredicate(List<Permanent> permanents) {
        this.permanents = permanents;
    }

    @Override
    public boolean apply(Permanent input, Game game) {
        return permanents.contains(input);
    }
}
