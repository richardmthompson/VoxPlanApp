# VoxPlan MVP Release Plan

**Target Release Date**: This Week (December 2025)
**Version**: 3.3-MVP
**Status**: Code Complete - Ready for Testing & Documentation

---

## Executive Summary

VoxPlan MVP focuses on the core value proposition: **Hierarchical Goal Management + Gamified Focus Sessions + Quota Progress Tracking**. Dailies and Schedule features have been hidden (not deleted) to simplify the initial release and gather user feedback on the core experience.

---

## MVP Scope: What's Included ✅

### Core Features (Shipped)

**1. Hierarchical Goal Management** ✅ COMPLETE
- Create, edit, delete goals with up to 3 levels of nesting
- Breadcrumb navigation for exploring goal hierarchy
- Reorder goals (vertical up/down, hierarchy up/down via ActionMode)
- Goal icons, colors, and customization
- **Files**: MainScreen.kt, MainViewModel.kt, GoalEditScreen.kt, GoalEditViewModel.kt, SharedViewModel.kt

**2. Daily Quotas** ✅ COMPLETE
- Set time quotas (minutes per day) for any goal
- Configure active days (Mon-Sun) for quota tracking
- Quota inheritance from parent goals
- Visual quota indicators in goal list
- **Files**: GoalEditScreen.kt (quota section), QuotaEntity.kt, QuotaRepository.kt

**3. Gamified Focus Mode** ✅ COMPLETE
- Timer with pause/resume functionality
- Medal system (Bronze/Silver/Gold/Diamond for 30/60/90/120+ min blocks)
- "Bank Time" feature to save earned medals to vault
- Visual power bar showing quota progress
- Pomodoro mode (work/rest cycles)
- Sound effects for medal awards and banking
- **Files**: FocusModeScreen.kt, FocusViewModel.kt

**4. Time Banking** ✅ COMPLETE
- Bank earned focus time to TimeBank (persistent storage)
- TimeBank entries tied to specific goals and dates
- Time contributions count toward daily quotas
- **Files**: TimeBankEntity.kt, TimeBankRepository.kt

**5. Progress Tracking** ✅ COMPLETE
- Weekly view of quota progress across all goals
- Visual bars showing time logged vs quota targets
- Navigate previous/next weeks
- Filter by goals with active quotas
- **Files**: ProgressScreen.kt, ProgressViewModel.kt

**6. Bottom Navigation** ✅ COMPLETE (MVP-Simplified)
- 2-tab navigation: **Goals** and **Progress**
- Clean, focused UI for core features
- **Files**: VoxPlanApp.kt, VoxPlanNavHost.kt

---

## MVP Launch Checklist 📋

### Code & Build ✅
- [x] Core features implemented (Goals, FocusMode, Progress, Quotas)
- [x] Recent bugs fixed (Dailies duplication, Add Quota duplication)
- [x] Dailies/Schedule hidden from navigation
- [x] Build succeeds (`./gradlew assembleDebug`)
- [x] No compilation errors
- [ ] Manual testing on device/emulator

### Testing (In Progress)
- [ ] **Manual Test 1**: Launch app → Verify 2 bottom nav items (Goals, Progress)
- [ ] **Manual Test 2**: Create goal with quota → Enter FocusMode → Bank time → Exit
- [ ] **Manual Test 3**: Navigate to Progress → Verify banked time appears
- [ ] **Manual Test 4**: Test goal hierarchy (create parent/child, navigate breadcrumbs)
- [ ] **Manual Test 5**: Test reordering (ActionMode vertical/hierarchy buttons)
- [ ] **Smoke Test**: No crashes in core flows

### Documentation (To Do)
- [ ] **README.md**: Overview, features, installation instructions
- [ ] **USER_GUIDE.md**: How to use VoxPlan (getting started, core workflows)
- [ ] **CHANGELOG.md**: Version history, what's new in MVP
- [ ] **LICENSE**: Choose and add license file
- [ ] Update CLAUDE.md with MVP scope
- [ ] Screenshots for README (Goals, FocusMode, Progress screens)

### Branding & Assets (To Do)
- [ ] App logo/icon design
- [ ] App name finalization (VoxPlan or alternative?)
- [ ] Color scheme/theme validation
- [ ] Sound assets review (medal sounds, banking sound)

### Repository Prep (To Do)
- [ ] Create GitHub repository (public)
- [ ] Push codebase to GitHub
- [ ] Set up GitHub README with screenshots
- [ ] Add .gitignore (if not already present)
- [ ] Tag MVP release (v3.3-mvp)

### App Store Prep (To Do)
- [ ] **Google Play Store**:
  - [ ] Create developer account (if not exists)
  - [ ] App listing (title, description, keywords)
  - [ ] Screenshots (5-8 images)
  - [ ] Feature graphic
  - [ ] Privacy policy
  - [ ] Content rating questionnaire
  - [ ] Build signed release APK/AAB (`./gradlew bundleRelease`)
  - [ ] Upload to Play Console
  - [ ] Submit for review

- [ ] **Alternative: F-Droid** (Open source option):
  - [ ] Prepare F-Droid metadata
  - [ ] Submit to F-Droid repository

### Marketing & Outreach (To Do)
- [ ] Post in "Building in Public" community
- [ ] Create social media posts (Twitter, Reddit, etc.)
- [ ] Product Hunt submission (optional)
- [ ] Hacker News Show HN post (optional)
- [ ] Email existing contacts/beta testers

---

## Success Metrics (Post-Launch)

**Week 1 Goals**:
- [ ] 10 users install the app
- [ ] 5 users create at least one goal with quota
- [ ] 3 users complete a focus session
- [ ] 0 critical bugs reported

**Month 1 Goals**:
- [ ] 50 users install the app
- [ ] Gather user feedback on core features
- [ ] Identify most-requested features

---

## Technical Debt & Known Issues

**Minor Issues (Non-blocking for MVP)**:
1. Unused warnings in build (acceptable)
2. Deprecated icon usage (List, ArrowForward icons) - cosmetic
3. Database index missing on QuotaEntity.goalId (performance, not critical)
4. QuickScheduleScreen.kt entirely commented out (can delete)

**Intentional Decisions**:
- Event entity kept in database (no migration to remove)
- Daily/Schedule ViewModels kept in codebase (dormant)
- No unit tests yet (acceptable for MVP)

---

## Release Timeline Estimate

**Assuming 1-2 hours per day**:

| Task | Time | Status |
|------|------|--------|
| Manual testing | 1 hour | ⏳ To Do |
| README.md | 1 hour | ⏳ To Do |
| USER_GUIDE.md | 2 hours | ⏳ To Do |
| Screenshots | 1 hour | ⏳ To Do |
| Logo/branding | 2 hours | ⏳ To Do |
| GitHub setup | 30 mins | ⏳ To Do |
| Play Store listing | 2 hours | ⏳ To Do |
| Build signed release | 30 mins | ⏳ To Do |
| Submit for review | 30 mins | ⏳ To Do |
| **TOTAL** | **~11 hours** | |

**Realistic Launch Date**:
- **If focused**: 3-4 days (working 3-4 hours/day)
- **If casual**: 1-2 weeks (working 1-2 hours/day)

---

## Version History

**v3.3-MVP** (Target: December 2025)
- Core features: Goals, FocusMode, Progress, Quotas, TimeBank
- Hidden: Dailies, Schedule
- Focus: Simplicity, core value proposition, initial user feedback

**v3.2** (December 2025)
- Dailies improved with parent/child Events
- Full feature set (pre-MVP scope reduction)

**v3.1** (November 2025)
- Focus Mode with quota integration
- Medal system and time banking

---

## Contact & Support

**Developer**: Richard Thompson
**Repository**: [GitHub URL - To Be Added]
**Issues**: [GitHub Issues URL - To Be Added]
**Documentation**: See README.md and USER_GUIDE.md

---

## Notes

- **Philosophy**: Ship fast, gather feedback, iterate based on real usage
- **Decision**: Hide complexity (Dailies/Schedule) to focus users on core value (Goals + Focus + Progress)
- **Flexibility**: All deferred features are code-complete and easily re-enabled
- **Priority**: Get app in users' hands this week, learn what they actually need

**Last Updated**: 2025-12-22
**Next Review**: After initial user feedback (Week 1 post-launch)
