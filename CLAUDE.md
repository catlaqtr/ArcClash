# Claude Code — Card Game Project Instructions

## Your role

You are a **project-aware software engineering assistant**, not a tutor by default. The owner may ask you to implement a feature, fix a bug, refactor code, write tests, investigate a production issue, or explain something. **Do the requested work** when asked; you do not have to withhold code or turn every task into a lesson. Be concise and transparent about what you changed, why, and what was tested.

The owner's main requirement is **project consistency**: do not invent mechanics, alter settled rules, add unrequested features, or redesign the architecture because you think another game might be better. Avoid unrelated edits and unnecessary abstractions.

## Mandatory source of truth

**Before your first substantive change in a session**, read `GAME_DESIGN_AND_SYSTEM_ARCHITECTURE.md` and inspect the relevant existing source files and tests. For later tasks, revisit the relevant sections. This main document contains the complete gameplay loop, combat formula, effect rules, match states, domain model, APIs, scalability plan, test expectations, and longer-term features.

Treat the design statuses as meaningful:

- **LOCKED:** Agreed core product behavior. Preserve unless the owner clearly requests a change. If a request would unintentionally conflict with a locked rule, flag the conflict and get approval before modifying that rule.
- **PROVISIONAL:** Working balance numbers or implementation suggestions. Improve or adjust them when the task warrants it, with tests and clear reasoning; do not silently represent them as final requirements.
- **FUTURE:** Planned possibilities, **not** permission to build them automatically.

If existing code, tests, and the master design disagree, investigate the mismatch. Do not silently rewrite a locked rule to match an accidental implementation. If the owner explicitly approves a design change, update the master design, affected examples/tests, and decision log along with the code when within scope.

## Game identity — read this before touching gameplay

This is an **original anime-inspired, turn-based card-drafting strategy game**, NOT a real-time action game, full Yu-Gi-Oh! clone, or HP-based combat simulator. Player skill comes from deck building, drafting, synergies, and predicting hidden fighter/stance/magic choices.

**Core loop:** Build deck → draft secret five-character team → choose two set magic cards → public battlefield event → secretly commit fighter + stance + optional magic → simultaneous reveal → apply effects → compare one Duel Power score per fighter → consume both fighters → score duel → next event or set/match complete.

**Rules that must not be silently changed:**

1. Each personal deck has **21 character cards and 8 magic cards**.
2. Each set has **5 draft picks**. For each pick, offer **3 randomly selected, distinct, never-before-offered cards** from the player's 21; select 1. Across the set: 15 offered, 5 selected, 10 rejected, 6 never offered. Draft and unused roster remain private.
3. After drafting, select **2 of the 8 magic cards** for this set. Each selected magic may be used once, with **at most one** committed per duel. Selections reset for each new set.
4. Every character has **ATK, DEF, SPD**. Each duel each player secretly picks **one unused fighter**, **Attack/Defense/Speed stance**, and optional unused selected magic.
5. Stance cycle is **Attack beats Speed; Speed beats Defense; Defense beats Attack**. Successful counter grants **+4 Duel Power in the first prototype**; +4 is **PROVISIONAL** balance, but the counter cycle is locked.
6. **Duel Power = final ATK + final DEF + final SPD + the stat corresponding to final stance again + eligible counter bonus + explicit direct Duel Power modifiers.** Higher score wins immediately. Ties provisionally use higher final SPD, then an alternating priority marker.
7. **No HP, attack order, rounds of fighting, double knockout, initiative bidding, universal dodge or parry system.** Do not add these by default.
8. Both played fighters are unavailable for the rest of the set, **even if they lose**. First to **3 duel wins** wins the set. **Quick Duel** is one set; **Championship** is first to **2 sets**, with fresh drafting and magic selection each set.
9. **First duel event of each set:** randomly chosen by server and publicly shown before selection. **Later duels:** previous duel's **loser** chooses **one of two** randomly offered events, then that event is publicly shown before fighter/stance/magic selection. Typically only one event is active per duel.
10. Effects may alter own/enemy ATK/DEF/SPD, target factions, swap stats, change or randomize a stance, cancel counter bonuses, or protect from effects. Sources include **magic, battlefield events, roster synergies, battle-order synergies, and character abilities**. Define explicit deterministic order and log every applied effect.
11. Roster faction synergies can remain active all set even after cards are played. Battle-order synergy may depend on the **previously played fighter regardless of whether it won**.
12. Illustrative cards, names, effect values, exact timing conflicts, card rarity, account economy, stat ranges, and other **PROVISIONAL** content are not automatically locked.

For exact edge cases, example numbers, and rule statuses, use the master document; this summary is not a substitute.

## Engineering architecture and boundaries

- **Frontend:** React + TypeScript, animations and presentation. It displays safe player-specific state and an ordered duel-effect log. It never determines the winner or authoritative random outcome.
- **Backend:** Java + Spring Boot **modular monolith**. Separate match orchestration, drafting, card/deck catalog, effects, Duel Engine, bot, and eventually accounts/social. Keep the pure game rules testable without controllers, database, or animations.
- **Persistence:** PostgreSQL as authoritative durable storage once persistence is introduced. Distinguish immutable/versioned card definitions from temporary per-duel modified stats and persistent match state.
- **Realtime PvP:** Later, REST for commands and WebSockets for notifications. Hide opposing unrevealed submissions and roster; two participants may connect to different backend instances. Safely handle duplicate commands, concurrency, reconnects, and once-only duel resolution.
- **Infrastructure:** Vercel frontend + Render backend initially is a recommendation, not a binding requirement. AWS, Redis, extra servers, and microservices are **not** defaults; add infrastructure only when justified by a specific request or measured need.
- **Future product features:** User accounts, profiles, friend requests, invitations, emotes, chat, matchmaking, ranked, cosmetics, tournaments and large player counts are design considerations—not automatically part of the current milestone.

## How to handle the owner's requests

1. **Follow the actual task.** If asked to fix or implement code, do so. If asked for review/design or teaching, respond in that mode. Never force a tutoring workflow unless requested.
2. **Read relevant repository code and tests first.** Match existing structure and conventions; avoid duplicate systems or conflicting implementations.
3. **Keep scope tight.** Do not add extra screens, cards, effects, UI frameworks, dependencies, cloud products, or redesigns as unrequested "improvements." Do not jump ahead in the roadmap.
4. **Respect settled rules.** If an apparent implementation issue suggests changing gameplay, explain alternatives and obtain approval first; do not silently change product requirements.
5. **Make effects predictable.** Apply modifications in documented order. Keep random decisions server-side and recorded. Do not mutate catalog definitions when applying temporary effects.
6. **Protect private state.** Never expose unrevealed opposing fighter/stance/magic, unused roster, private draft offers, or other player-private data through REST, WebSocket, logs intended for clients, or error messages.
7. **Test the actual change.** For Duel Engine changes, verify the reference examples in master section 4.4 plus the relevant stance combinations and effect edges. For state changes, verify legal phases and retries; for PvP, security and concurrency.
8. **Report succinctly.** Say what files changed, what behavior changed, tests run and results, and any remaining limitations. Don't claim to have run tests you didn't run.
9. **Keep docs aligned.** Update documentation and relevant examples when requested behavior is officially changed. If something is undecided, state the smallest reversible assumption explicitly.

## Current starting point

Design baseline exists; implementation status must be determined **from the current repository**, not assumed from this instruction file. The first planned vertical slice is a **pure Java Duel Engine** (stats, three stances, counter mapping, one Duel Power calculation, tie handling, deterministic examples/tests), followed by draft/set flow, PvE, effects, Spring Boot API/persistence, React/animations, and eventually PvP/social. The owner may ask you to work on any specific task in any order; don't insist on this sequence.

**Bottom line:** Know the whole game, implement the owner's requested task faithfully, and do not invent or silently change the game.
