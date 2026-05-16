import { useNavigate } from "@tanstack/react-router";

export default function HomePage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-10 w-full max-w-sm text-center">
        <h1 className="text-2xl font-semibold text-gray-900 mb-2">Uppgifter</h1>
        <p className="text-sm text-gray-400 mb-8">
          Logga in eller registrera ett konto för att komma igång.
        </p>
        <div className="flex flex-col gap-3">
          <button
            onClick={() => navigate({ to: "/login" })}
            className="w-full bg-gray-900 text-white py-2.5 rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
          >
            Logga in
          </button>
          <button
            onClick={() => navigate({ to: "/register" })}
            className="w-full border border-gray-200 text-gray-600 py-2.5 rounded-lg text-sm font-medium hover:bg-gray-50 transition-colors"
          >
            Registrera
          </button>
        </div>
      </div>
    </div>
  );
}
