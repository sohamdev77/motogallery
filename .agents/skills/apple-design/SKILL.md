---
name: apple-design
description: Apple's approach to interface design and fluid, physical motion, translated for the web and modern native platforms. Use when building or reviewing gesture-driven UI, spring animations, drag/swipe/sheet interactions, momentum and interruptible transitions, translucent materials and depth (glassmorphism), typography (optical sizing, tracking, leading), reduced-motion, or the design foundations (feedback, spatial consistency, restraint) behind Apple-style interfaces.
---

# Apple Design

How Apple builds interfaces that stop feeling like a computer and start feeling like an extension of you. This knowledge comes from Apple's WWDC design talks — chiefly *Designing Fluid Interfaces* (WWDC 2018) — distilled and translated into modern UI platforms (CSS/Web, Jetpack Compose, Pointer Events, spring physics).

The through-line: an interface feels alive when motion starts from the current on-screen value, inherits the user's velocity, projects momentum forward, and can be grabbed and reversed at any instant. Springs are the tool that makes all of this natural, because they are inherently interruptible and velocity-aware.

## The Core Idea
> "When we align the interface to the way we think and move, something magical happens — it stops feeling like a computer and starts feeling like a seamless extension of us."

An interface is fluid when it behaves like the physical world: things respond instantly, move continuously, carry momentum, resist at boundaries, and can be redirected mid-motion.

Apple frames design as serving four human needs: safety/predictability, understanding, achievement, and joy.

---

## 1. Response — Kill Latency
The moment lag appears, the feeling of directness "falls off a cliff." Response is the foundation everything else is built on.

- **Respond on pointer-down, not on release.** Highlight a button or compress a card the instant it's pressed. Waiting for click/touch-up to show feedback feels dead.
- **Be vigilant about latency.** Audit debounces, artificial timers, transition waits, and tap delays.
- **Feedback must be continuous** during the interaction, not just at the end. For a drag, slider, or drawer, update the UI 1:1 with the pointer the whole way through.

---

## 2. Direct Manipulation — 1:1 Tracking
> "Touch and content should move together."

When the user drags something, it must stay glued to the finger — and respect the offset from where they grabbed it. Snapping to the element's center on grab breaks the illusion immediately.

- Track position + timestamp history to compute velocity at release.
- Avoid lag between finger coordinate and UI response.

---

## 3. Interruptibility — The Single Most Important Principle
> "The thought and the gesture happen in parallel."

Every animation must be interruptible and redirectable at any moment. A user must be able to grab a moving element mid-flight and reverse it without waiting for the animation to finish.

- Never lock out input during a transition.
- Always animate from the presentation (current live) value, never the target value.
- Avoid hard cuts at reversals; blend velocities smoothly.
- Decompose 2D motion into independent X and Y springs.

---

## 4. Behavior Over Animation — Use Springs
> "Think of animation as a conversation between you and the object, not something prescribed by the interface."

Apple deliberately replaced the physics triplet (mass/stiffness/damping) with two designer-friendly parameters:

1. **Damping Ratio**: controls overshoot.
   - `1.0` = critically damped (no overshoot, smooth settle).
   - `< 1.0` = underdamped (overshoots and oscillates slightly).
2. **Response**: how quickly the value reaches the target (in seconds). Lower = snappier.

### Recommended Values:
- **Default UI spring / Reposition / Menus**: Damping `1.0`, Response `0.3–0.4s` (no overshoot).
- **Momentum / Flick interaction**: Damping `~0.8`, Response `0.3–0.4s` (slight bounce because a flick preceded it).
- **Drawer / Sheet**: Damping `0.8`, Response `0.3s`.

---

## 5. Velocity Handoff
When a gesture ends, the animation must continue at the finger's exact velocity, so there's no visible seam between dragging and animating.

$$\text{relativeVelocity} = \frac{\text{gestureVelocity}}{\text{targetValue} - \text{currentValue}}$$

---

## 6. Momentum Projection
> "Take a small input and make a big output."

Don't snap to the nearest boundary from the release point. Use velocity to project the resting position — exactly like scroll deceleration — then snap to the target nearest that projected point:

$$\text{projectedEndpoint} = \text{currentPosition} + \frac{v}{1000} \cdot \frac{d}{1 - d} \quad (d \approx 0.998)$$

---

## 7. Spatial Consistency
> "If something disappears one way, we expect it to emerge from where it came."

- Enter and exit along symmetric paths.
- Anchor interactions to their source (`transform-origin` at trigger).
- Mirror easing curves on reversible transitions.

---

## 8. Rubber-Banding — Soft Boundaries
At an edge, resist progressively instead of stopping hard. A hard stop reads as "frozen"; continuous resistance reads as "responsive, but there's nothing more here."

$$\text{rubberband}(x, d, c = 0.55) = \frac{x \cdot d \cdot c}{d + c \cdot |x|}$$

---

## 9. Materials & Depth — Translucency & Glassmorphism
Apple uses translucent materials as a floating functional layer that brings structure without stealing focus:

- **Translucent Chrome**: Build nav/toolbars/sheets as translucent layers (blur + semi-transparent tint) with content scrolling underneath — not opaque bars consuming a fixed strip.
- **Specular Edge Highlights**: Fine 1px/0.5dp top border with a bright specular gradient (`rgba(255, 255, 255, 0.2)` down to `0.03`) catching the light.
- **Material Weight Encodes Hierarchy**: Heavier materials separate structural regions; lighter materials draw attention to interactive elements.
- **Vibrancy**: Over blurred/translucent surfaces, maintain high contrast and optical legibility.

---

## 10. Typography — Optical Sizing, Tracking, Leading
- **Tracking (letter-spacing)**: Size-specific. Large display text wants negative tracking (`-0.02em` or `-0.5sp`); body copy stays neutral (`0sp`).
- **Leading (line-height)**: Tight on large display titles, proportional and comfortable on body.
- **Weight**: Build hierarchy from weight + size + leading as a set.

---

## 11. Multimodal Feedback & Tactile Response
- **Causality**: Obvious cause and physical alignment.
- **Harmony**: Visual, sound, and haptics trigger on the exact same frame.
- **Utility**: Reserve feedback for meaningful tactile moments.
