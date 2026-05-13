"use client";

import Lottie from "lottie-react";
import animationData from "../../../public/running-cat.json";

interface CatLoaderProps {
  /** Size of the animation container in px. Default: 200 */
  size?: number;
  /** Optional label shown below the cat. Default: "Loading…" */
  label?: string;
}

export function CatLoader({ size = 200, label = "Loading…" }: CatLoaderProps) {
  return (
    <div
      className="flex flex-col items-center justify-center gap-3 py-16"
      role="status"
      aria-label={label}
    >
      <Lottie
        animationData={animationData}
        loop
        autoplay
        style={{ width: size, height: Math.round(size * (391 / 681)) }}
      />
      <p className="text-sm font-medium text-muted-foreground animate-pulse">
        {label}
      </p>
    </div>
  );
}
