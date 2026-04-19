#ifndef EventKitConstants_h
#define EventKitConstants_h

#import <EventKit/EventKit.h>

// Expose EventKit enum constants to Kotlin/Native
static const EKEntityType EKEntityTypeEventValue = EKEntityTypeEvent;
static const EKEntityType EKEntityTypeReminderValue = EKEntityTypeReminder;
static const EKSpan EKSpanThisEventValue = EKSpanThisEvent;
static const EKSpan EKSpanFutureEventsValue = EKSpanFutureEvents;

// Helper functions to get enum values
static inline EKEntityType getEKEntityTypeEvent(void) {
    return EKEntityTypeEvent;
}

static inline EKEntityType getEKEntityTypeReminder(void) {
    return EKEntityTypeReminder;
}

static inline EKSpan getEKSpanThisEvent(void) {
    return EKSpanThisEvent;
}

static inline EKSpan getEKSpanFutureEvents(void) {
    return EKSpanFutureEvents;
}

#endif /* EventKitConstants_h */
