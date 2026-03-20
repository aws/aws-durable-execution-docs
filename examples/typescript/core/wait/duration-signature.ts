type Duration =
  | { days: number; hours?: number; minutes?: number; seconds?: number }
  | { hours: number; minutes?: number; seconds?: number }
  | { minutes: number; seconds?: number }
  | { seconds: number };
