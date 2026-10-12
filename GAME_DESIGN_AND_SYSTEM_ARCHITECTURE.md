# Card-Drafting Duel Game — Game Design & System Architecture

**Version:** 1.1 — design baseline (Claude Code collaboration update)  
**Last updated:** 2026-10-10  
**Project name:** Working title only; no official name selected  
**Status:** Product concept and agreed combat rules; implementation has not started  
**Primary audience:** The project owner, a senior-developer mentor, and coding assistants such as Claude Code  
**Intended stack:** Java + Spring Boot, PostgreSQL, React + TypeScript, Docker  

> **READ THIS FIRST — SOURCE OF TRUTH**
>
> This document is the current agreed design for an original, anime-inspired, strategic online card game. **Do not silently change the core gameplay, card counts, duel formula, stance cycle, or event-selection flow.** Distinguish **LOCKED** gameplay choices from **PROVISIONAL** balancing or implementation proposals and **FUTURE** ambitions. If a requested implementation conflicts with a locked rule, explain the conflict and ask before changing the design. Build a small functioning version before implementing future-scale infrastructure.
>
> **Working with coding assistants:** This document is the product/architecture source of truth, not a restriction on implementation help. The owner may personally practice coding or explicitly ask Claude Code to implement, fix, debug, refactor, or test a feature. Follow the current request. Claude Code is **not required to act as a tutor**. Avoid unsolicited changes, preserve locked game rules, inspect the existing repository, and keep requested edits scoped and tested. See `CLAUDE.md` for operational instructions.

---

## 1. Executive summary

### 1.1 Product vision

A turn-based, anime-inspired **deck-building + hidden-draft + prediction** card duel game. Players build personal decks, draft a secret five-character team, benefit from faction and battle-order synergies, then repeatedly select a hidden fighter, one of three stances, and optional magic. Players reveal together; battlefield events, abilities, magic, and synergies modify the values; **one simple Duel Power calculation** determines the winner of each duel. No HP bar and no combat simulation.

The game should be easy to learn and satisfying to watch, but difficult to master through deck design, draft decisions, hidden predictions, and timing of limited magic.

### 1.2 Three sources of player skill

1. **Deck building:** Construct 21-character + 8-magic decks with useful combinations.
2. **Drafting:** Choose the strongest five-character team from randomly offered trios; consider roster synergies and playable order.
3. **Prediction:** Choose a fighter, stance, and limited magic that will beat what the opponent might secretly select.

### 1.3 Core flow — LOCKED

**Build deck → Enter match → Draft five fighters → Select two magic cards → Reveal current event → Secretly commit fighter/stance/optional magic → Reveal both sides → Resolve effects → Calculate Duel Power → Declare winner → Choose next event (loser chooses from two) → Continue until first to three duel wins → New set if Championship.**

### 1.4 Non-goals

This is **not** a physics-based fighting game, real-time action game, or full Yu-Gi-Oh! rules clone. There are no health bars, damage-over-time loops, initiative bidding, attack turns, parries/dodges as universal base mechanics, or double KOs in the agreed combat design. Individual future card effects may introduce new twists, subject to explicit design approval.

---

## 2. Decision register — what is fixed vs adjustable

| Decision | Status | Details |
|---|---|---|
| 21 characters + 8 magic in personal deck | **LOCKED** | Per deck; restrictions on duplicate copies are not finalized |
| Draft five fighters in five separate picks | **LOCKED** | Each pick offers 3 randomly drawn, non-repeating character cards from the player's 21-card deck |
| Draft secrecy | **LOCKED** | Do not expose opponent's draft choices, unused roster, or unplayed choices |
| Two selected magic cards per set | **LOCKED** | Chosen from the 8; each usable once, maximum one per duel |
| 3 character stats | **LOCKED** | Attack (ATK), Defense (DEF), Speed (SPD) |
| 3 secret stances | **LOCKED** | Attack, Defense, Speed; selected with fighter |
| Counter cycle | **LOCKED** | Attack beats Speed; Speed beats Defense; Defense beats Attack |
| One instant Duel Power result | **LOCKED** | No HP or multi-round damage simulation |
| Duel Power formula | **LOCKED as baseline** | Final ATK + final DEF + final SPD + final selected-stance stat + counter bonus |
| Counter bonus amount | **PROVISIONAL** | +4 Duel Power in the first prototype; balance-test rather than treating as immutable |
| Tie resolution | **PROVISIONAL accepted baseline** | Higher final SPD; if still tied, alternating priority marker |
| One fighter each duel; both used | **LOCKED** | Both committed fighters become unavailable for the rest of that set |
| First duel battlefield event | **LOCKED** | Random event, announced before fighter selection |
| Following duel event | **LOCKED** | Previous duel's **loser** picks 1 of 2 randomly offered events; announced before next selections |
| Faction and battle-order synergies | **LOCKED concept** | Exact effects and numbers are provisional |
| Events, magic, abilities manipulate stats/stances/rules | **LOCKED concept** | Individual effects and balancing are provisional |
| Quick Duel | **LOCKED** | One set; first to 3 duel wins, maximum 5 duels |
| Championship | **LOCKED** | First to 2 sets; fresh draft + magic selection at each set |
| PvE first, PvP later | **LOCKED implementation direction** | Rules engine must work for both |
| Social systems and rank | **FUTURE** | Friends, invitations, emotes, chat, profiles, ranked, cosmetics, tournaments |
| 50k users / 2k concurrent players | **TARGET scenario, not measured capacity** | Design for reasonable scalability; benchmark before sizing |
| React / Java / PostgreSQL / Vercel / Render | **RECOMMENDED**, not permanent | Can migrate backend infrastructure to AWS later |

**Change control:** If a rule changes, update this table, its detailed section, test examples, and a dated decision-log entry. Do not implement only a partial change.

---

## 3. Gameplay — complete rules

### 3.1 Card and deck construction

Each player's saved deck contains **21 character cards** and **8 magic cards**. A character definition has an ID, name, art asset reference, faction(s), ATK, DEF, SPD, and optional traits and character-specific abilities. Magic cards have IDs, names, effect descriptions, timing, and effect definitions. Initial illustrative factions: **Ninja, Pirate, Mage, Knight**; future factions and tags are expected.

**Provisional MVP restriction:** One copy of any given character or magic card ID per deck. This makes drafting without replacement straightforward, but duplication rules are an open design decision and must not be presented as already agreed.

An active match snapshots the deck/card/rules versions used at match creation; editing a saved deck later must not alter an ongoing match.

### 3.2 Match formats

- **Quick Duel:** One set; winner is first player to win 3 individual duels (up to 5 duels).
- **Championship:** Best of 3 sets; first player to win 2 sets wins the match. Start each set with the **same saved 21+8 deck snapshot**, but generate fresh draft offers and allow a fresh selection of 2 magic cards.
- **PvE (first release):** Human versus computer opponent, following the same legal action and visibility rules.
- **PvP (future):** Human versus human, two secret commitments, synchronized reveal, reconnection and forfeit handling.

Because each duel always declares exactly one winner, a 3-win set winner is guaranteed within five duels.

### 3.3 Drafting — LOCKED

For **each set, per player**:

1. Start with the 21 available character cards in that player's deck.
2. Server randomly presents **three unique, not-yet-offered cards**.
3. Player selects exactly one of those three.
4. All three offered cards are removed from the **draft-offer pool for that set**; two rejected cards cannot return in a later offer in the same set.
5. Repeat for exactly five picks. This generates the **five-character roster**.
6. Across five offers, exactly **15 distinct characters were offered**: 5 selected, 10 offered but rejected, 6 never offered.
7. Drafted roster and rejected/offered options are private to that player; a player may see their own full roster.

For PvP, both players can draft independently. The server must not reveal the opponent's roster during drafting or while it is unplayed. The server decides the random offers and validates each pick.

### 3.4 Magic selection — LOCKED

- After drafting (per set), each player selects exactly **2 of the 8 magic cards** in their saved deck.
- During a duel, they may secretly choose **zero or one** of those two magic cards.
- A selected magic card may be committed **once per set**; magic consumed in one duel is unavailable in later duels of that set.
- In the next Championship set, magic selections and usage reset, so the player can choose two again.
- **Recommended initial behavior:** A committed card is consumed even if its effect gets blocked; document this visibly if adopted.
- Unused and selected magic must remain secret until legally revealed.

### 3.5 Battlefield events — LOCKED

**Every duel has one public battlefield event.**

- **Duel 1 of each set:** Server randomly chooses the event from the eligible event pool and displays it to both players **before** they submit their fighter/stance/magic.
- **Duel 2 onward:** Previous duel's **loser** receives **two distinct random event choices**, chooses one, then the selected event is shown to both players before their next secret selections.
- The new event **replaces** the previous duel's event; normally its effects last only that duel.
- If a set has ended, don't offer another event.
- Event options should be generated and stored server-side so a repeated request cannot reroll them.
- Event options may bias the game toward the loser as a comeback mechanic. Test for deliberate-loss exploits.

Example event ideas (not final cards):

| Event | Possible behavior |
|---|---|
| Blood Moon | All fighters gain +2 ATK |
| Heavy Fog | All fighters lose 2 SPD, floor at zero |
| Knight's Kingdom | Knight fighters +3 DEF; Pirate fighters −1 ATK |
| Wind Festival | Ninja fighters +1 SPD |
| Chaotic Battlefield | Swap both fighters' current ATK and DEF |
| Magic Seal | Suppress specified types of magic effect for this duel |

### 3.6 Secret duel selection — LOCKED

Once the event is public, each player secretly commits:

1. **One unused fighter** from their five-character roster.
2. Exactly one stance: **Attack, Defense, Speed**.
3. **Optional** one unused selected magic card.

Opponents may know that the other player has **submitted/ready**, but may not see the fighter, stance, or magic until the server is ready to reveal both. Neither submission can be edited after final commit in the recommended PvP model; allow editing before confirming in the UI.

Once both commitments exist, the server validates/resolves the duel **once**, determines the winner, updates scores, and marks both fighters used. The duel result can reveal cards, stances, committed magic, and the actual effects/results relevant to the duel, but not unplayed roster cards.

### 3.7 End of duel, set, and match

- Exactly **one** duel winner is declared; no double knockout or draw.
- Winner gets 1 duel victory; loser receives no point.
- Both fighters are marked used for the rest of the set regardless of winner.
- Previous-fighter battle-order synergy can depend on the last fighter played, **even if that fighter lost**, unless a particular synergy explicitly says otherwise.
- At three duel victories, the set ends immediately.
- In Championship, check whether someone has won two sets; if not, start a fresh set with new draft and magic selection.

---

## 4. Combat rules — agreed Duel Power model

### 4.1 Three character attributes — LOCKED

- **ATK / Attack:** Offensive specialization; especially valuable when selecting Attack Stance.
- **DEF / Defense:** Defensive specialization; especially valuable when selecting Defense Stance.
- **SPD / Speed:** Agility specialization; especially valuable when selecting Speed Stance and provisionally serves as first tie-breaker.

Each stat contributes to the general power total; the active stance adds its matching stat **one extra time**. There is **no subtraction of opposing Defense**, no HP, and no attack order in this design.

### 4.2 Stances and counter cycle — LOCKED

| Stance chosen | Extra stat counted | Counters |
|---|---|---|
| Attack | ATK | Speed |
| Defense | DEF | Attack |
| Speed | SPD | Defense |

When your stance counters the opponent's **final stance**, your fighter receives **+4 Duel Power** in the prototype. The loser of the stance interaction does not receive a penalty; it simply misses out on the bonus. Same stance versus same stance: neither gets a counter bonus.

**Critical:** Reevaluate counters **after** stance-changing effects have resolved. If a card forces a new stance, the Duel Power duplicate-stat choice changes too.

### 4.3 Duel Power calculation — LOCKED formula, PROVISIONAL numeric bonus

**Final Duel Power = Final ATK + Final DEF + Final SPD + Final Stat Matching Your Final Stance + Stance Counter Bonus + Any Explicit Direct Duel-Power Modifiers.**

1. Start with the original character stats, then apply valid effect modifications in a deterministic sequence.
2. If magic/events swap stats, compute the **final modified stats** as the actual current values.
3. If an effect changes a stance, use the **final stance**.
4. Compute the base total ATK + DEF + SPD.
5. Add the stat matching the final stance a second time.
6. Add the counter bonus (+4 in first prototype) if final stance counters the opponent's final stance.
7. Apply any explicitly defined direct Duel Power modifiers at their specified timing.
8. Compare each side's final Duel Power; the greater number wins.

**Tie rule (provisional baseline):** If Duel Powers tie, higher **final SPD** wins. If final SPD also ties, the predefined **alternating priority marker** determines the winner. Proposed priority marker setup: assign holder randomly at set start, then switch holder after every duel, so its use is fair and reproducible. This initial assignment/rotation is a proposed exact implementation rule, not a historical agreement.

No negative stat values in the MVP: stat debuffs floor individual stats at 0. Discuss maximums/caps during balancing; avoid arbitrary universal caps without tests.

### 4.4 Reference calculations (check future code against these)

**Example characters** (illustrative, not fixed cards):

| Fighter | ATK | DEF | SPD | Total |
|---|---:|---:|---:|---:|
| Shadow Ninja | 8 | 3 | 7 | 18 |
| Iron Knight | 5 | 9 | 4 | 18 |

**Example A: Ninja Attack versus Knight Defense**
- Ninja = 18 + 8 + 0 = **26**.
- Knight = 18 + 9 + 4 (Defense counters Attack) = **31**.
- **Knight wins, 31–26**.

**Example B: Ninja Speed versus Knight Defense**
- Ninja = 18 + 7 + 4 (Speed counters Defense) = **29**.
- Knight = 18 + 9 + 0 = **27**.
- **Ninja wins, 29–27**.

**Example C: Ninja Speed versus Knight Attack**
- Ninja = 18 + 7 + 0 = **25**.
- Knight = 18 + 5 + 4 (Attack counters Speed) = **27**.
- **Knight wins, 27–25**.

**Example D: Multiple sources changing a duel**
- Start Ninja Attack = 26 and Knight Defense = 31.
- Event adds **+1 SPD to Ninja**. Since Ninja chose Attack Stance, Duel Power increases **by 1**, so Ninja = 27.
- Roster synergy adds **another +2 SPD to Ninja**. Again Attack Stance: +2 Duel Power; Ninja = **29**.
- Ninja's magic swaps the Knight's **current ATK and DEF**. Knight changes from ATK 5 / DEF 9 / SPD 4 to ATK 9 / DEF 5 / SPD 4. The Knight's Defense Stance now duplicates DEF 5: 9 + 5 + 4 + 5 + 4 counter = **27**.
- **Ninja wins 29–27**. Show these steps in the UI.

**Double-counting example:** If a Speed-Stance fighter gets +2 SPD, their Duel Power increases **by 4** (once in the base sum and once as the doubled stance stat), unless an effect expressly changes this rule. That behavior is intentional and important for balancing.

### 4.5 Balancing warnings

- A high-specialization character usually wants to double its largest stat; the stance-counter bonus must be strong enough to make predicting the opponent meaningful, but not so strong it erases card identity.
- Stat buffs aligned with the selected stance are especially valuable due to double-counting.
- Faction and magic combinations can stack. Test stacking ceilings and avoid accidental near-guaranteed wins.
- Avoid consistently stronger rare cards. Competitive balance should reward decisions, not collection spending.
- **+4 is a playtest starting number**, not proof of balance.

---

## 5. Effects, magic, synergies, abilities, and events

### 5.1 One reusable effect concept

Magic, events, character abilities, and synergies are **sources** of effects, not four separate rule engines. Start with an extensible but small vocabulary:

| Effect category | Example | Implementation direction |
|---|---|---|
| Stat additive modifier | +3 ATK to self; −2 SPD to opponent | First effect type to implement |
| Faction-targeted modifier | Knights gain +2 DEF | Apply only if target has matching faction/tag |
| Stat transformation | Swap ATK and DEF | Make order versus buffs explicit |
| Stance manipulation | Force Defense; choose random different stance | Compute final stance before counter check |
| Counter-rule change | Disable +4 counter bonus | Apply in specific rule-modifier phase |
| Protection/cancellation | Block one harmful stat debuff | Intercept target effect; decide consumption |
| Direct power modifier | +2 final Duel Power | Explicit, distinct from stat changes |

Represent each effect conceptually as: **source + trigger/timing + optional condition + target + operation + magnitude/parameters + duration + stacking/priority rules + visibility**. Do not build a fully general scripting language or unrestricted plugin runtime in version one.

### 5.2 Roster synergies — LOCKED concept

Activate according to the **five drafted fighters**, not only those that remain unused. Bonuses persist for the whole set, including after fighters are consumed, unless a future effect explicitly overrides that behavior.

Illustrative examples: 2 Ninjas → +1 SPD for Ninja fighters; 3 Ninjas → total +2 SPD for Ninja fighters; 2 Knights → +1 DEF for Knights. Whether the 3-unit threshold replaces the 2-unit effect or stacks with it must be specified per synergy (recommended: highest achieved tier replaces lower tier unless deliberately cumulative).

The actual composition is hidden from the opponent, even if the opponent can infer something from effects they see after reveals.

### 5.3 Battle-order synergies — LOCKED concept

Effects can trigger from the **faction/trait of the fighter played in the previous duel** into the current fighter, regardless of whether the previous fighter won. Illustrative transitions:

- Ninja → Pirate: Surprise Raid, +2 ATK to current Pirate.
- Pirate → Ninja: +2 SPD to current Ninja.
- Mage → Knight: +2 DEF to current Knight.
- Knight → Mage: +2 ATK to current Mage.

Synergies apply only if conditions are actually met and are logged in duel results. First duel has no previous played fighter. Exact values are provisional.

### 5.4 Illustrative magic (not finalized content)

- **Berserker's Rage:** Your fighter +3 ATK.
- **Armor Corrosion:** Opponent −3 DEF (not below zero).
- **Mirror Curse:** Swap opponent's current ATK and DEF.
- **Confusion:** Change opponent's stance to one of the other two, selected server-side randomly.
- **Stance Breaker:** Prevent opponent from receiving the stance-counter bonus.
- **Spell Shield:** Negate one eligible incoming harmful effect.

Balance caution: changing a stance can simultaneously change the doubled stat **and** the stance-counter result. Therefore it may be more impactful than a small stat buff; rarity alone is not a balancing mechanism.

### 5.5 Deterministic resolution order — PROVISIONAL detailed contract

The original gameplay requires consistent order, but precise conflicts need playtesting. Use this **candidate v1 timing contract** and update it intentionally as new effects are introduced:

1. **Validate** match phase, both legal committed selections, available fighters/magic, and active event.
2. **Create mutable duel snapshots** from immutable versioned character definitions; do not edit the card catalog or player's saved deck.
3. **Apply roster and battle-order passives**, then public battlefield event modifiers, recording each effect.
4. **Reveal simultaneous commitments** to the rules engine (not necessarily to clients yet); queue character abilities and committed magic effects at their defined timing/priority. Apply simple additive effects with reliable ordering.
5. **Resolve protection/cancellation** when a targeted effect is about to apply (not as an unrelated bonus at the end).
6. **Resolve defined transformations**, such as swapping the current ATK/DEF values. In the candidate v1 contract, swaps occur after ordinary additive modifiers; thus buffs can move with the swapped value. This must be documented consistently on the affected cards.
7. **Resolve stance changes**, including any server-selected random outcome. The final stance overrides the submitted stance for scoring only; preserve both original and final stances in the log.
8. **Apply eligible rule changes** affecting counter bonuses or final Duel Power.
9. **Calculate Duel Power** on both final state snapshots, including counter outcomes and tie-breaks.
10. **Return a detailed resolution log** for animation and explanations; atomically persist winner, consumed cards, and updated match scores.

For same-phase effects with multiple sources, define a stable tie-break such as explicit priority followed by source/type/ID; **never depend on unordered database retrieval or hash-map iteration**. Avoid secret choices being exposed to clients before both have committed. If later abilities need earlier/later timing, add clear trigger phases rather than rewriting the existing formula.

**Random effects:** Server chooses and records the actual outcome for reproducible debugging/replays; never let the client determine random results.

### 5.6 Explanation-driven design

Every duel outcome should support a user-facing, step-by-step explanation:

- Original fighter stats and chosen stances.
- Each event, synergy, ability, and magic that actually applied, with amount/target.
- Stats and stances before/after transformations.
- Which stance countered which and the granted bonus.
- Final Duel Power for both fighters, tie-break if relevant, and winner.

The backend is authoritative for this log; React animates it and may offer **fast/reduced animations**.

---

## 6. Product UI / screens / animation design

### 6.1 Core screens (MVP)

1. **Main menu:** Play PvE, saved progress if enabled, settings.
2. **Deck overview / builder:** Show selected 21 character + 8 magic cards (may start with a fixed sample deck).
3. **Draft screen:** Three selectable character cards, own drafted roster, five picks, selected/rejected visual feedback.
4. **Magic selection:** Choose two from eight.
5. **Event announcement/selection:** First random; later previous loser selects one of two; show chosen event to both sides.
6. **Battlefield:** Visible event, own remaining roster, own remaining magic, own fighter + stance choice, opponent readiness indicator, set score.
7. **Reveal and duel resolution:** Cards flip/reveal; modifiers animate; stance counter and final Duel Power appear; winner announced.
8. **Set and match result:** Score, winner, replay of effect explanation, next set if Championship.

### 6.2 Animation contract

The **Spring Boot Duel Engine resolves the duel**, then returns both outcome and a **sequence of typed presentation-friendly events** (for example REVEAL_CHARACTER, APPLY_STAT_MODIFIER, SWAP_STATS, CHANGE_STANCE, APPLY_COUNTER_BONUS, SHOW_POWER, DECLARE_WINNER). React uses these to animate in a sensible order; Java does **not** perform animations.

Visual ideas: card hover, draft selection highlight, face-down slide, 3D card flip, spell glow, animated number change, stat swap, stance-counter emblem, winner glow, loser fade. Sounds and particles are optional enhancements. Skip/fast animation and reduced-motion accessibility must be considered. A player should be able to understand the calculation without watching the full animation.

### 6.3 Frontend principles

- Use React + TypeScript, a component-based design, Tailwind/CSS, and **Motion for React** for card animations when needed.
- Responsive layouts: desktop first for productivity, mobile-friendly for players.
- Distinguish **public match state** from **player-private state** in frontend models.
- Frontend may preview a hypothetical Duel Power for the player's own choice, but must not assume opponent's unseen data or declare official winners.
- Never fetch an opponent's unpublished selection/roster and merely hide it with CSS. Private data **must not be present in the API response**.
- Card images and other art use optimized static assets/object storage/CDN; use original or properly licensed artwork.

---

## 7. Recommended high-level architecture

### 7.1 Architectural style

**Modular monolith first:** One Java Spring Boot deployable divided into modules/packages with defined responsibilities. A single PostgreSQL database. React is a separate frontend deployment. Avoid premature microservices, Kafka, Kubernetes, or a giant generic effect engine.

**Conceptual flow:**

Player browser (React) → HTTPS REST / later WebSocket → Spring Boot (validation, state machine, rules) → PostgreSQL (authoritative persistence). Image assets served through static hosting/CDN. As traffic grows, several identical backend instances can handle different connections; state must not live only in any one instance's memory.

### 7.2 Backend modules (logical, not microservices)

| Module | Main responsibility |
|---|---|
| Catalog & Rules | Card/faction/event definitions, ruleset versions, abilities and effect metadata |
| Deck & Collection | Validate and manage saved 21+8 decks; future ownership/collection |
| Match Orchestration | Match creation, phases, scores, sets, turn legality, surrender/reconnect |
| Draft | Random non-repeat offers and picks, private 5-card roster |
| Magic Selection | Secret selection and per-set usage tracking |
| Event Selection | Random first event; loser gets 2 choices for subsequent duel |
| Duel Engine | Pure/repeatable duel computation; no HTTP concerns |
| Effect Resolver | Apply buffs, swaps, stance changes, cancels, and produce effect log |
| PvE Opponent | Chooses legal actions using only computer-private and public information |
| Identity & Profiles (later) | Authentication, profile, stats, privacy |
| Social & Invitations (later) | Friends, blocks, game invitations, emotes, chat policies |
| Realtime Notifications (later) | Safe WebSocket event delivery; no game authority in socket layer |

The **Match Orchestrator** invokes Draft/Event/Magic/Duel modules, and persists state. The **Duel Engine** should not call REST controllers, render UI, or send WebSocket messages.

### 7.3 Recommended tools

- **Backend:** Java (supported LTS release), Spring Boot, Spring Security when accounts arrive, Spring Data JPA where practical; keep pure domain logic easy to unit-test.
- **Database:** PostgreSQL; **Flyway** or equivalent for migrations; SQL indexes based on queries, not guesses.
- **Testing:** JUnit, Spring Boot Test, integration tests with Testcontainers/PostgreSQL when appropriate, concurrency/load tests later.
- **Frontend:** React + TypeScript (Vite is fine), CSS/Tailwind, Motion for React for animation.
- **Dev/deployment:** Git, GitHub, Docker, GitHub Actions, environment-specific configuration and secrets.
- **Initial hosting:** Vercel for React, Render for containerized Spring Boot, managed PostgreSQL. **AWS is an optional future migration, not a prerequisite**.
- **Later:** WebSockets for real-time PvP/social; Redis-compatible Pub/Sub or a reliable message broker if cross-instance delivery becomes necessary. Add monitoring, CDN/object storage, and autoscaling as evidence requires.

---

## 8. Domain model and ownership boundaries

Do not confuse an immutable **card definition** with a player's **owned card**, a **drafted roster card**, or a **temporary duel snapshot**.

### 8.1 Main domain objects

| Concept | Purpose / suggested data |
|---|---|
| CharacterDefinition | Stable character ID, name, faction/tags, base ATK/DEF/SPD, ability references, artwork, card version |
| MagicDefinition | Stable ID, name, typed effects, description, version |
| EventDefinition | Stable ID, name, typed effects, eligibility, version |
| Faction/Tag | Stable ID, visible name, membership rules; don't hard-code only four factions forever |
| EffectDefinition | Type, parameters, target, condition, timing, duration, stacking, priority |
| Deck | Owner, version, character/magic selections and validation status |
| Match | ID, mode, ruleset/catalog snapshot ID, two participants, status, match winner, version |
| MatchParticipant | Player/bot identity, seat, stats of match participation, private information ownership |
| GameSet | Match ID, ordinal, phase, score, set winner, priority marker, selected magic per player |
| DraftState | Per-player offer pool, current offered trio, pick number, selected roster |
| RosterEntry | Card definition snapshot/reference, owning player, used flag, order played |
| EventOffer | Two offered event choices, eligibility, chooser (previous loser), resulting selection |
| Duel | Set ID, ordinal, active event, status, both sides' resolution output, winner |
| DuelSubmission | Player, fighter roster entry ID, chosen stance, optional magic, committed flag/time, command ID |
| DuelResolution | Original/final stats, original/final stance, final power, winner, ordered explanations/random outcomes |
| PlayerProfile (later) | Identity, display name, avatar, user preferences, statistics, cosmetics |
| Friendship / Block (later) | Direction/status, requester/recipient, timestamps, privacy |
| GameInvitation (later) | Requester, invitee, match type, state, expiration, resulting match ID |
| Chat / Emote (later) | Sender, receiver/match, rate-limit/visibility, moderation state where applicable |

### 8.2 Separation of state

- **Catalog state:** Defined cards, effects, and rules. Versioned; not mutated by a duel.
- **Persistent player state:** Accounts, collections, saved decks, profiles, friends.
- **Match state:** Draft offers, roster, magic usage, event choice, scores, turns, hidden commits; saved to survive restart.
- **Derived duel state:** Temporary effective stats/stance calculated from match input. Persist final resolution for history, but do not overwrite base card stats.

### 8.3 Relationships (conceptual)

Player has many Decks; a Deck has exactly 21 character selections and 8 magic selections. Match has two Participants and one or more Sets. Each Set has two DraftStates and two magic selections, plus 1–5 Duels. Each Duel has two private submissions and one result; results have many ordered EffectLog entries. Public/private serialization is based on requesting participant, phase, and reveal status.

---

## 9. PostgreSQL persistence plan

### 9.1 Table groups (planning, not rigid migrations)

**Catalog:** character_definitions, magic_definitions, event_definitions, factions, character_factions, effect_definitions, rulesets/version references.  
**Players/decks:** user_accounts (later), player_profiles, decks, deck_character_entries, deck_magic_entries, collection_entries (later).  
**Matches:** matches, match_participants, game_sets, draft_states, draft_offers, draft_picks, roster_entries, selected_magic_entries, event_offers, duels, duel_submissions, duel_resolution_logs.  
**Social:** friend_requests/friendships, blocks, game_invitations, chat_messages as needed later.

Don't create every table on day one. A small prototype can start with in-memory definitions and in-memory match orchestration, then add persistence before public PvP. When persistence arrives, matches and secret submissions need durable storage.

### 9.2 Essential data guarantees

- IDs and foreign keys ensure submissions point to the correct participant, match, set, and unused fighter.
- Unique constraints prevent duplicate use of a roster fighter/magic within a set and duplicate final resolution of one duel (design specific constraint keys carefully).
- Store **ruleset/card version snapshots or sufficient historical resolved data** so catalog balancing changes never retroactively rewrite recorded outcomes.
- Persist active match progress so a backend restart/reconnect can resume.
- Use transactions and concurrency control when accepting submissions and resolving duels.
- Index actual access paths: active matches by player, match's sets/duels, unexpired invitations, and later username/friend lookups.
- Store art/media references rather than huge image binary data directly in relational rows.

### 9.3 Persistence boundary for MVP vs PvP

**Prototype:** In-memory game engine and fixed card catalog are acceptable for learning.  
**Before real PvP:** Persistent match state, auth, transactional duel resolution, idempotent actions, and hidden-data filtering are **mandatory**.

---

## 10. Match state machine and legal actions

### 10.1 Suggested states

**CREATED → DRAFTING → MAGIC_SELECTION → EVENT_SELECTION / EVENT_ANNOUNCED → AWAITING_DUEL_SUBMISSIONS → RESOLVING_DUEL → DUEL_RESULT → [next EVENT_SELECTION or SET_COMPLETE] → [next DRAFTING or MATCH_COMPLETE].**

First event selection is server-generated; subsequent event selection is performed by the previous duel's loser. To simplify the UI, EVENT_SELECTION can represent both server-random and player-choice subtypes with authorization rules.

**At all states:** User can only access their own allowed private data, and can view permitted public match data. A surrendered/expired/completed match cannot accept another gameplay action.

### 10.2 Transition table

| Current phase | Allowed operation | Condition | Next phase |
|---|---|---|---|
| CREATED | Start match | Valid deck snapshot and opponent/bot | DRAFTING |
| DRAFTING | Request/receive current trio, pick one | Correct participant; card in current offer; each needs 5 picks | DRAFTING or MAGIC_SELECTION |
| MAGIC_SELECTION | Select 2 magic | Exactly 2 eligible unused deck magic; both sides complete | EVENT_SELECTION |
| EVENT_SELECTION (first duel) | Draw random event | Server only | AWAITING_DUEL_SUBMISSIONS |
| EVENT_SELECTION (later duel) | Loser selects one of two offers | Correct loser; option offered by server | AWAITING_DUEL_SUBMISSIONS |
| AWAITING_DUEL_SUBMISSIONS | Commit fighter, stance, optional magic | Participant, valid unused resources, correct phase | Same phase until both ready; then RESOLVING_DUEL |
| RESOLVING_DUEL | Resolve and persist | Server only; never resolve same duel twice | DUEL_RESULT |
| DUEL_RESULT | Advance | If no side has 3 wins | EVENT_SELECTION |
| DUEL_RESULT | Complete set | A side has 3 wins | SET_COMPLETE |
| SET_COMPLETE | New set | Championship, neither side has 2 sets | DRAFTING |
| SET_COMPLETE | Finish match | Quick Duel or Championship winner decided | MATCH_COMPLETE |

### 10.3 Failure handling

- Reject commands in wrong phase with clear, non-leaking errors.
- Treat duplicate submitted commands safely; repeated calls should not duplicate consumption, scores, or matches.
- Do not expose opponent's selections when one side has committed and the other hasn't.
- Persist the reason/effect log for duel resolution, including any randomness used.
- For PvP reconnect, authenticate participant and return the correct **viewer-specific snapshot**, not a generic entity serialization.

---

## 11. API and realtime communication plan

These are **conceptual contracts**, not finalized HTTP URLs or response structures. Determine exact fields after designing domain states and authentication.

### 11.1 REST actions (suggested)

| Action | Suggested endpoint | What it does |
|---|---|---|
| List catalog | GET /api/cards, /api/magic, /api/events | Public card definitions or eligible collection |
| Manage deck | GET/POST/PUT /api/decks | Validate 21+8 saved deck |
| Start PvE match | POST /api/matches/pve | Creates match + opponent + deck snapshot |
| Get my match view | GET /api/matches/{id} | Public + viewer-authorized private state only |
| Draft pick | POST /api/matches/{id}/draft/picks | Select exactly one card from current offered trio |
| Select magic | POST /api/matches/{id}/sets/current/magic | Commit the two per-set magic cards |
| Choose event | POST /api/matches/{id}/sets/current/events/choice | Only previous duel loser, only offered option |
| Submit duel choices | POST /api/matches/{id}/duels/current/submission | Commit fighter, stance, optional magic |
| Duel/result/history | GET /api/matches/{id}/duels/current/result | Show resolved result and effect log once permitted |
| Surrender | POST /api/matches/{id}/surrender | End/forfeit according to match rules |
| Friends (later) | /api/friends and /api/friend-requests | Manage requests and relationship |
| Invitations (later) | /api/invitations | Create/accept/decline/expire game challenges |

**Write-actions must carry** authentication (once accounts exist), expected match/turn version or equivalent concurrency guard, and an idempotency/command ID for important submissions. Return errors without leaking hidden enemy choices.

### 11.2 Future real-time events

REST commands should remain authoritative; WebSockets are mainly for pushing state changes. Suggested notifications: MATCH_UPDATED, OPPONENT_READY, EVENT_OPTIONS_AVAILABLE (only to chooser), EVENT_SELECTED, DUEL_RESOLVED, SET_COMPLETED, MATCH_COMPLETED, INVITATION_RECEIVED, EMOTE_RECEIVED, CHAT_MESSAGE_RECEIVED.

**Message visibility is recipient-specific.** An OPPONENT_READY notice conveys readiness only, not fighter ID/stance/magic. Even in the WebSocket layer, never broadcast an entire match object containing both players' secrets.

### 11.3 Animation data

A resolved duel response should include official winner, both revealed cards/stances, final stats, final power, counter details, and an **ordered list of typed effect steps with previous/next values**. The frontend uses this list for replay/animations and UI explanations.

---

## 12. PvP correctness, concurrency, security

### 12.1 Two-player simultaneous commitment

1. Alice authenticates and privately submits a legal choice.
2. Backend validates, persists, and marks Alice ready without leaking details.
3. Bob submits separately; his backend request may reach **another Spring Boot instance**.
4. When both are committed, a transactional workflow ensures **only one resolver** claims that duel.
5. Apply deterministic effects and record result, consume both fighters/committed magic, update score and version atomically.
6. Notify both players with authorized result data. A notification failure must not undo the committed game result.

Consider optimistic locking with a `version` field or database row locks, plus unique constraints on resolved duel/command IDs. Choose one consistent strategy after understanding actual concurrent writes. Avoid trusting “I have checked it is not resolved” without an atomic guard.

### 12.2 Security must-haves

- Server **owns** random offers/events, validates card ownership/decks, calculates all effects and winner.
- Check **match membership, turn ownership, action permission, and resource availability** on every gameplay request; authentication alone is not authorization.
- Never include opponent's unplayed roster, offered draft cards, committed stance, or magic in responses before reveal.
- Do not use client-supplied combat totals or trust submitted modified stats.
- Validate payloads, authenticate sessions securely, use TLS, implement rate limits for social/chat and sensitive actions.
- Later: password/account recovery, profile privacy, blocking/muting/reporting, retention/deletion policy, abuse detection.

### 12.3 Disconnects, timeouts, reconnects

Match and selections persist, not just Java memory. On reconnect, user requests viewer-specific current match state. Future PvP needs explicit selection timers, idle limits, surrender/forfeit, and abandoned-game handling. **Exact timeout lengths and whether a timeout auto-plays or forfeits are OPEN decisions.** An important rule: do not reveal a secret choice as part of recovery before both commits.

### 12.4 PvE fairness

Computer opponent sees **its own** hidden state and **public** state, but must not inspect the human's unrevealed fighter/stance/magic or draft options to select counters. Start with a simple rule-based or weighted-random legal-action strategy. Improve difficulty later without cheating.

### 12.5 Server restart and deployments

Preserve ongoing matches in PostgreSQL before production PvP. Provide health checks, graceful shutdowns, reconnect paths, database backup/restore exercises, and deployment rollback. Rule/version snapshot avoids changing active match behavior mid-game when balances change.

---

## 13. Scale and cloud plan

### 13.1 Scale target — design assumption

**50,000 registered users** and **2,000 simultaneously connected/playing** represent a possible future scenario, not current measured load. If all 2,000 are in two-player PvP matches, that is about **1,000 concurrent matches**. This is a turn-based game; server work is mainly validations, state writes, websocket messages, and small deterministic power calculations. **Benchmark before estimating instance size or AWS/Render bills.**

### 13.2 Progressive hosting architecture

| Stage | Suggested deployment |
|---|---|
| Learning/first prototype | React + Spring Boot + PostgreSQL locally (Postgres/Docker when ready) |
| Public early PvE | React on Vercel, Spring Boot Docker on Render, managed PostgreSQL |
| First PvP | Same stack with auth, transactional state, REST commits and WebSocket notifications |
| Growth | Multiple Spring Boot instances, pooled DB connections, tuned indexes, shared pub/sub, monitoring, load tests |
| More complex ops / economics | Evaluate AWS ECS/Fargate, ALB, RDS PostgreSQL, ElastiCache, S3/CloudFront, etc. |

**Do not commit to AWS at the start solely because it can scale.** Use clear infrastructure boundaries, containerized builds, managed DB, and environment settings to make migration practical later.

### 13.3 Why Redis may become useful

With multiple backend instances, Alice can be connected to instance A and Bob to instance B. A pub/sub or message broker helps deliver live notifications between instances. **Persist critical state in PostgreSQL first; Redis Pub/Sub messages can be lost while subscribers are offline.** Friend requests, invitations, match results, and important chat messages should not rely exclusively on ephemeral pub/sub.

### 13.4 Scalability measurement plan

Measure: peak active WebSocket connections, commands/sec, duel resolves/sec, p95/p99 command latency, database query time/pool saturation, CPU/RAM, error rates, dropped realtime notifications, cost per active match, reconnection success. Test at small scale, then at simulated 100/500/2,000 concurrent connected players. Build realistic action patterns; don't infer that connection count equals requests per second. Define production SLOs after first measurements.

### 13.5 Assets and costs

Keep original or properly licensed artwork and spell assets in CDN-backed/static storage, not in PostgreSQL binary columns. Optimize image sizes and cache policies: at high audience size, **media bandwidth may cost more than duel calculations**. Monitor hosting and bandwidth costs and set spend alerts.

---

## 14. Future social, progression, matchmaking, and operations

These are **intended growth directions, not MVP obligations**.

### 14.1 Profiles, friends, invitations

- Player profile: public display name, avatar/banner, favorite faction (optional), match stats, cosmetic customization, opt-in privacy settings.
- Friend workflow: request, accept, decline, remove; prevent duplicate requests; blocking takes precedence over invitations/contact.
- Invitation workflow: invite friend to Quick Duel/Championship, accept/decline/expire, create **one** match transactionally after acceptance. An invitation must not create multiple matches due to repeated acceptance.
- Presence: online/offline/in-match; presence is approximate, temporary and separate from permanent profile data.

### 14.2 Emotes and chat

Start with a predefined in-match emote set (GG, applause, surprise, laugh, etc.). Add user mute/block and rate limiting. Free-text chat can follow later, with report, spam controls, moderation policies, retention, user privacy, and regional legal considerations. Chat and emotes are not required for duel correctness.

### 14.3 Matchmaking, ranking, collection, monetization

Casual matchmaking and friend invitations before ranked. Ranked may need rating, seasons, leaver penalties, and anti-abuse. Collections can expand with additional characters/magic and original cosmetic art. Prefer skill-based competitive fairness; avoid mandatory paid stat superiority. If monetization/younger audiences are considered later, separately review platform, privacy, consumer-protection, and moderation requirements.

### 14.4 Operations

Logs, metrics, alerts, error tracking, database backups and restore tests, secret management, CI/CD, migrations, deploy/rollback documentation, data retention/deletion options, user abuse response. Add monitoring early enough to debug production issues; don't overbuild a dedicated operations platform in the MVP.

---

## 15. Testing strategy and acceptance criteria

### 15.1 Unit tests — core rules

- All 3 stances double **only** their matching final stat.
- The stance-counter mapping is exactly Attack→Speed, Speed→Defense, Defense→Attack; same stance yields no counter.
- Counter bonus goes only to countering side, not both.
- Examples A/B/C/D from section 4.4 reproduce expected numbers and winners.
- If final powers tie, compare final SPD, then priority marker; never yield an unresolved duel.
- Stat changes in one duel do not mutate the shared card definition or a later duel.
- Mirror Curse changes the **current** target stats and affected stance contribution; randomized stance changes are recorded.
- +2 SPD on Speed Stance increases Duel Power by 4 (barring explicit overrides).
- Negative debuffs floor stats at 0 in the candidate v1 policy.

### 15.2 Match/draft tests

- Deck rejects wrong card counts; valid 21+8 deck passes.
- Each five-offer sequence consists of 15 distinct cards; five selected, ten rejected, six unseen.
- A player cannot select a card not in the current trio.
- No later offer can include a previously offered card during the same set.
- Five roster cards are private to owner; already used fighter cannot be committed again.
- Player selects exactly two magic cards per set; each can be used once, at most one per duel.
- First event random; after duel, event chooser is exactly previous loser; only one of two offered values accepted.
- Event is shown to both before fighter selection; prior event replaced.
- First to three duels wins; Championship ends at two sets; fresh set resets draft/magic and uses same deck snapshot.
- Roster synergy persists even after roster member used; battle-order synergy uses last played fighter even after loss.

### 15.3 Integration/security/concurrency tests

- A player cannot read or act on another match even when authenticated.
- Opponent ready notification does not contain unrevealed data.
- Duplicate action submission does not double-consume a card or score a duel twice.
- Two last simultaneous submissions across separate requests result in **exactly one** duel resolution.
- Match can be loaded after restart from persisted state.
- Stale client version receives a safe conflict and can refresh.
- PvE agent does not inspect human hidden information.
- Versioned old match result remains reproducible after changes to current card catalog.

### 15.4 Usability/gameplay tests

- A new player can tell what each stance means and which counters which.
- In duel results, players can explain **why** they won/lost (event, synergy, magic, selected stance, counter, power).
- Multiple archetypes have viable choices; high-stat cards do not always dominate.
- Stance counter +4 is neither irrelevant nor an unavoidable automatic win.
- Test whether losing-player event choice creates fair comebacks or perverse incentives.
- Card artwork and final Duel Power readable on mobile; reduced-motion mode works.

---

## 16. Development roadmap — implement thin vertical slices

**Guiding principle:** Each milestone delivers an observable working result and its tests. Avoid building a huge infrastructure without a playable match. The owner can implement personally, collaborate with a mentor, or instruct Claude Code to make scoped changes. The current request controls the assistance mode; the game requirements remain the same.

### Milestone 0 — Preparation and scope

- Read and approve this master plan. Create Git repo, README, issue/backlog tracking, backend/frontend folders as needed.
- Decide a working title, which basic card definitions are available, and keep all *unapproved* content provisional.
- Choose project conventions (Java version, build tool, TypeScript, code style) and a consistent test strategy.
- **Done when:** Repo builds, tests can run, rules baseline is documented.

### Milestone 1 — Pure Java Duel Engine (no web UI)

- Model stats, stance, fighter card definition vs duel snapshot, and result.
- Implement simple three-stat Duel Power and counter cycle; implement tie resolution.
- Test the reference examples and every stance matchup; prevent mutation of card definitions.
- **Done when:** Two fighters and two stances produce correct reproducible result and explanation **without** Spring MVC/controllers or database.

### Milestone 2 — Set and draft mechanics

- Define 21-card pool, five rounds of 1-from-3 without replacement, and five-card roster.
- Implement set score, used fighter rules, best-of-five duels, first to three.
- **Done when:** A full set can be played using simple text/testing interface with valid state transitions.

### Milestone 3 — Computer opponent and playable PvE

- Bot drafts and chooses only from its own/private and publicly available information.
- Implement first random event and subsequent loser-choice options (even if initial event effects are simple).
- **Done when:** Human can complete a Quick Duel against the bot with no hidden-state cheating.

### Milestone 4 — Expand effect system incrementally

- Start with additive stat modifiers from factions and events.
- Add roster synergy and battle-order synergy.
- Add two magic selections and per-set consumption.
- Add stat swap and stance-change operations, with strict timing and effect-log tests.
- **Done when:** A duel with event, roster synergy, magic, transformation, counter bonus resolves and explains each step correctly.

### Milestone 5 — Spring Boot REST + PostgreSQL persistence

- Expose appropriate safe state-transition APIs.
- Persist catalog/version references and active match state with transactions/migrations.
- Add idempotency/concurrency protection and API authorization appropriate to PvE stage.
- **Done when:** Match survives a server restart and no illegal/duplicate action corrupts the score.

### Milestone 6 — React UI + animation

- Draft, magic, event, battlefield, stance selection, duel result UI.
- Cards flip/slide; effect timeline highlights modifications; final power/winner display.
- Fast/reduced motion and mobile-readable layout.
- **Done when:** Nontechnical player can complete Quick Duel in browser and understand every result.

### Milestone 7 — Deck building, Championship, public PvE deployment

- Saved deck validation for 21+8; Championship best-of-three sets.
- Docker/dev setup, CI, initial Vercel/Render deployment, managed database, monitoring and backup plans.
- **Done when:** Publicly playable end-to-end PvE with documented setup and tested core rules.

### Milestone 8 — Secure multiplayer foundation

- Accounts/auth, server-side visibility, two human commits, WebSocket notifications, reconnects/timeouts.
- Friend invitations can be a follow-up after simple PvP.
- **Done when:** Two independent browsers play valid PvP without leaks or double resolution, even with retries.

### Milestone 9 — Social and competitive growth

- Profiles, friends, invitations, presence, emotes; later chat, casual queue, ranked.
- Only add multi-instance and shared message infrastructure after baseline multiplayer is correct and load metrics justify it.
- **Done when:** Features meet security/moderation rules, remain optional for core gameplay, and don't couple chat to the Duel Engine.

**Note:** Milestone order can adapt. For example, a simple frontend can appear earlier to keep development enjoyable; do not sacrifice correctness for an elaborate animation first.

---

## 17. Suggested repository documentation

Recommended top-level files/directories (names are recommendations, not required implementation):

- **GAME_DESIGN_AND_SYSTEM_ARCHITECTURE.md** — this source of truth.
- **CLAUDE.md** — shorter instructions for AI collaborators.
- **README.md** — how to run, test, and understand the project.
- **docs/decisions/** — short dated Architecture Decision Records (ADRs) for contested choices.
- **docs/card-catalog/** — original card stats/effects and playtest data as content grows.
- **backend/** — Spring Boot modular monolith.
- **frontend/** — React app and animation UI.
- **infra/** — Docker/dev environment and deployment config when needed.

Do not create empty directories/packages merely to imitate an enterprise product. Build according to milestones.

---

## 18. Open decisions and explicit recommended defaults

These are NOT locked; they should be decided when implementation reaches the relevant milestone.

| Open item | Recommended first prototype default | Why / risk |
|---|---|---|
| Allowed duplicates per deck | Unique character IDs and unique magic IDs | Simpler non-repeat drafting; can revisit |
| Stat range and equal-budget policy | Begin with several roughly equal-sum archetypes (e.g., total 18) | Test fairness; not a permanent content restriction |
| Counter bonus amount | +4 | Established illustrative calculation; needs statistical/play tests |
| Initial tie-priority owner | Random at each set start, alternate after each duel | Resolves all ties fairly, reproducibly |
| Negative stat values | Floor at 0 | Avoids unintuitive negative power |
| Two effect sources targeting same stat | Additive modifiers sum, transformations in explicit later phase | Easy explanation; watch extreme stacks |
| Same-phase effect ordering | Explicit priority, then stable source identifiers | Determinism instead of accidental iteration order |
| Magic consumed if blocked | Yes once committed | Predictable resource cost |
| Event option restrictions | Two distinct eligible events, offered once per duel | Prevent reroll abuse; repetition across duels remains undecided |
| Timeouts/forfeits | Define during PvP milestone | Must not allow infinite waiting |
| Disconnections | Reconnect to persisted match; temporary grace period | Avoid accidental instant forfeits |
| Ruleset updates | Pin match to a snapshot/version | Prevent balance updates altering live matches |
| Character ability count/complexity | Start with zero or a few easy, typed effects | Avoid prematurely complex effect engine |
| Card collection economy | Begin with all cards available for PvE | Prioritize gameplay and learning |
| Matchmaking/social | Friends/invites first, chat later | Smaller moderation burden |
| Target age/content & artwork policies | Original content and age-appropriate community defaults | Avoid IP, moderation, and privacy problems |

### The first actual implementation decision

Start with the **Duel Engine and a complete 3×3 stance test matrix**. This is the smallest, most stable foundation of the project and does not depend on AWS, accounts, or artwork.

---

## 19. How Claude Code or another assistant should use this document

1. **Read this document before proposing architecture or modifying rules.** Treat it as the current game-design baseline.
2. Ask which milestone/task is being worked on and inspect existing code/tests before editing.
3. Explain whether a proposed change affects **LOCKED**, **PROVISIONAL**, or **FUTURE** design.
4. Preserve the **21+8 deck, 5-pick 1-from-3 draft, 2 magic per set, 3 stats, 3 countering stances, Duel Power formula, loser-choice event rule, and first-to-three duels** unless explicitly instructed otherwise.
5. Never move actual duel authority to React or reveal unpublished PvP selections to the wrong player.
6. **Assistant role:** Claude Code is a project-aware engineering collaborator, not a mandatory tutor. When asked to implement, fix, refactor, or test code, carry out the requested scoped change and report tests/results. When asked to teach or review, do that instead. Do not generate unrelated features or rewrite gameplay without approval.
7. Work incrementally; make a concrete acceptance checklist for each feature.
8. Keep an ADR/change log for rule changes; do not silently “fix” the gameplay by adding HP, turns, initiative, dodges, or other previously rejected combat systems.
9. Verify numerical examples and interactions rather than assuming a narrative explanation is mathematically correct.
10. If any detail is undecided, choose a small reversible implementation default, label it **PROVISIONAL**, and avoid inventing product requirements.

---

## 20. Decision log

- **2026-10-10 — Gameplay baseline:** Adopted 3 stats (ATK, DEF, SPD), 3 stances (Attack/Defense/Speed), rock-paper-scissors counters, one instant Duel Power calculation, provisional +4 stance counter bonus, and no HP/attack rounds.
- **2026-10-10 — Effects and events:** Magic, events, faction/battle-order synergies, and character abilities may modify stats, specific factions, stances, and duel rules; first event is random; after every duel the loser chooses one of two random events for the next duel.
- **2026-10-10 — Product direction:** PvE first, PvP later; possible player profiles, friends, invitations, emotes, chat, ranks, new cards/factions, and ambitions around 50k registered/2k simultaneous players.
- **2026-10-10 — Technical direction:** Java/Spring Boot modular monolith + PostgreSQL + React/TypeScript, initially Render/Vercel; server-authoritative rules and secret choices, scalable without premature microservices.
- **2026-10-10 — AI collaboration agreement (updated in v1.1):** The owner may code personally or explicitly delegate implementation, bug fixes, refactoring, and tests to Claude Code. Claude must know the whole project, follow the current task, respect locked game rules, avoid unsolicited redesigns, and keep project documentation synchronized.

---

## 21. Quick reference — rules in 60 seconds

**Deck:** 21 character cards + 8 magic cards.  
**Draft:** Each set, five times choose one of three distinct random, non-repeating offered characters → private 5-character roster.  
**Magic:** Select 2 of 8 per set; each can be used once; at most one magic per duel.  
**Event:** Duel 1 random public event; afterward previous loser picks one of two random offers for next duel, publicly before selections.  
**Choose:** Both players secretly submit one unused fighter, one stance (Attack/Defense/Speed), and optional magic.  
**Reveal:** Resolve effects (including stats and stance changes), then compare final power.  
**Formula:** ATK + DEF + SPD + chosen stance stat again + counter bonus (+4 provisional) + explicit direct-power changes.  
**Counters:** Attack > Speed > Defense > Attack.  
**Tie:** Higher final SPD; if tied, alternating priority marker (provisional exact behavior).  
**After duel:** Each committed fighter consumed; score updated; event selection or set end.  
**Win set:** First to 3 duel wins. **Win Championship:** First to 2 sets.  
**Backend:** Java is authoritative for randomness, hidden choices, all effects, and winner.  
**Frontend:** React renders game/animations from safe server-provided state and an effect timeline.
