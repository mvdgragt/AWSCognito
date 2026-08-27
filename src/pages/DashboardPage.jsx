import { useState } from "react";
import { useNavigate } from "@tanstack/react-router";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import {
  useDeleteAccount,
  useTodos,
  useAddTodo,
  useDeleteTodo,
  getAuthHeaders,
} from "../hooks/userTodos";

const BASE_URL = import.meta.env.VITE_API_URL;

export default function DashboardPage() {
  const [text, setText] = useState("");
  const [actionError, setActionError] = useState("");
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: todos, isLoading } = useTodos();
  const addTodo = useAddTodo();
  const deleteTodo = useDeleteTodo();
  const deleteAccount = useDeleteAccount();

  const { data: user } = useQuery({
    queryKey: ["currentUser"],
    queryFn: async () => {
      const headers = getAuthHeaders();
      const res = await fetch(`${BASE_URL}/auth/me`, { headers });
      if (!res.ok) throw new Error("Kunde inte hämta användare");
      return res.json();
    },
  });

  async function handleLogout() {
    if (isLoggingOut) return;
    setActionError("");
    setIsLoggingOut(true);
    await queryClient.cancelQueries();
    queryClient.clear();
    localStorage.removeItem("accessToken");
    localStorage.removeItem("idToken");
    localStorage.removeItem("refreshToken");
    setIsLoggingOut(false);
    navigate({ to: "/login" });
  }

  async function handleAdd(e) {
    e.preventDefault();
    if (!text.trim()) return;
    setActionError("");
    try {
      await addTodo.mutateAsync(text);
      setText("");
    } catch {
      setActionError("Kunde inte skapa uppgift just nu.");
    }
  }

  async function handleDeleteAccount() {
    const confirmed = window.confirm(
        "Är du säker? Ditt konto och all data raderas permanent.",
    );
    if (!confirmed) return;
    setActionError("");
    try {
      await deleteAccount.mutateAsync();
      await handleLogout();
    } catch {
      setActionError("Kunde inte ta bort kontot just nu.");
    }
  }

  return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-lg mx-auto px-4 py-10">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-xl font-semibold text-gray-900">Mina uppgifter</h1>
              {user && (
                  <p className="text-sm text-gray-400">{user.email}</p>
              )}
            </div>
            <button
                onClick={() => {
                  void handleLogout();
                }}
                disabled={isLoggingOut}
                className="text-sm text-gray-400 hover:text-gray-900 transition-colors"
            >
              {isLoggingOut ? "Loggar ut..." : "Logga ut"}
            </button>
          </div>

          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <form onSubmit={handleAdd} className="flex gap-2 mb-5">
              <input
                  value={text}
                  onChange={(e) => setText(e.target.value)}
                  placeholder="Ny uppgift..."
                  className="flex-1 px-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-200"
              />
              <button
                  type="submit"
                  disabled={addTodo.isPending}
                  className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors disabled:opacity-50"
              >
                {addTodo.isPending ? "..." : "Lägg till"}
              </button>
            </form>
            {actionError && (
                <p className="text-sm text-red-500 mb-4">{actionError}</p>
            )}

            {isLoading ? (
                <p className="text-sm text-gray-400 text-center py-6">Laddar...</p>
            ) : todos?.length === 0 ? (
                <p className="text-sm text-gray-400 text-center py-6">
                  Inga uppgifter än. Lägg till en ovan!
                </p>
            ) : (
                <ul className="flex flex-col divide-y divide-gray-50">
                  {todos?.map((todo) => (
                      <li
                          key={todo.todoId}
                          className="flex items-center justify-between py-3 px-2 rounded-lg hover:bg-gray-50 group"
                      >
                        <span className="text-sm text-gray-700">{todo.text}</span>
                        <button
                            onClick={() => deleteTodo.mutate(todo.todoId)}
                            disabled={deleteTodo.isPending}
                            className="text-xs text-gray-300 hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100 disabled:opacity-30"
                        >
                          Ta bort
                        </button>
                      </li>
                  ))}
                </ul>
            )}
          </div>

          <div className="mt-8 text-center">
            <button
                onClick={handleDeleteAccount}
                disabled={deleteAccount.isPending}
                className="text-xs text-gray-300 hover:text-red-400 transition-colors"
            >
              {deleteAccount.isPending ? "Raderar..." : "Ta bort mitt konto"}
            </button>
          </div>
        </div>
      </div>
  );
}