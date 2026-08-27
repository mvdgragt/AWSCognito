import { useState } from "react";
import { useNavigate, useSearch } from "@tanstack/react-router";

export default function ConfirmPage() {
  const { email } = useSearch({ strict: false });
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [confirmed, setConfirmed] = useState(false);
  const navigate = useNavigate();

  const handleConfirm = async (e) => {
    e.preventDefault();
    setError("");
    try {
      const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, code }),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message || "Bekräftelse misslyckades");
      }

      setConfirmed(true);
    } catch (err) {
      setError(err.message);
    }
  };

  if (confirmed) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 w-full max-w-sm text-center">
          <div className="w-12 h-12 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-6 h-6 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h1 className="text-xl font-semibold text-gray-900 mb-2">Konto skapat!</h1>
          <p className="text-sm text-gray-400 mb-6">
            Ditt konto är bekräftat. Du kan nu logga in.
          </p>
          <button
            onClick={() => navigate({ to: "/login" })}
            className="w-full bg-gray-900 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
          >
            Logga in
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 w-full max-w-sm">
        <h1 className="text-xl font-semibold text-gray-900 mb-2">Bekräfta konto</h1>
        <p className="text-sm text-gray-400 mb-6">
          En verifieringskod har skickats till {email}
        </p>
        <form onSubmit={handleConfirm} className="flex flex-col gap-3">
          <input
            type="text"
            placeholder="Ange 6-siffrig kod"
            value={code}
            onChange={(e) => setCode(e.target.value)}
            required
            className="w-full px-3 py-2.5 border border-gray-200 rounded-lg text-sm text-center tracking-widest focus:outline-none focus:ring-2 focus:ring-gray-200"
          />
          {error && <p className="text-red-500 text-sm">{error}</p>}
          <button
            type="submit"
            className="w-full bg-gray-900 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
          >
            Bekräfta
          </button>
        </form>
      </div>
    </div>
  );
}
