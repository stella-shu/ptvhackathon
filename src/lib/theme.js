export const glassPanel =
  "bg-white/80 backdrop-blur-xl border border-white/60 shadow-[0_22px_65px_-32px_rgba(236,72,153,0.55)]";

export const panelPadding = "px-4 py-3 md:px-5 md:py-4";

export const pillButton =
  "rounded-full px-5 py-2.5 font-semibold text-slate-700 shadow-[0_18px_35px_-25px_rgba(236,72,153,0.7)] transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-rose-300";

export const softInput =
  "mt-1 rounded-2xl border border-white/70 bg-white/80 px-3 py-2 text-sm shadow-inner shadow-rose-100/40 focus:border-rose-200 focus:ring-2 focus:ring-rose-200/80 outline-none transition";

export function mergeClasses(...classes) {
  return classes.filter(Boolean).join(" ");
}
