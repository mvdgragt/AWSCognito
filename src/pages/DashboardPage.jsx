import { useState } from "react";
import { signOut, getCurrentUser } from "aws-amplify/auth";
import { useNavigate } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import {
  useDeleteAccount,
  useTodos,
  useAddTodo,
  useDeleteTodo,
} from "../hooks/userTodos";

export default function DashboardPage() {
  const [text, setText] = useState("");
  const navigate = useNavigate();
  const { data: todos, isLoading } = useTodos();
  const addTodo = useAddTodo();
  const deleteTodo = useDeleteTodo();
  const deleteAccount = useDeleteAccount();
  async function handleDeleteAccount() {
    const confirmed = window.confirm(
      "Är du säker? Ditt konto och all data raderas permanent.",
    );
    if (!confirmed) return;
    await deleteAccount.mutateAsync();
    await signOut();
    navigate({ to: "/login" });
  }
  // Hämta aktuell användares e-post
  const { data: user } = useQuery({
    queryKey: ["currentUser"],
    queryFn: getCurrentUser,
  });
  async function handleLogout() {
    await signOut();
    navigate({ to: "/login" });
  }
  async function handleAdd(e) {
    e.preventDefault();
    if (!text.trim()) return;
    await addTodo.mutateAsync(text);
    setText("");
  }
  return (
    <div>
      <header>
        <h1>Mina todos</h1>
        {user && <p>Inloggad som: {user.signInDetails?.loginId}</p>}
        <button onClick={handleLogout}>Logga ut</button>
      </header>
      <form onSubmit={handleAdd}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Ny todo..."
        />
        <button type="submit" disabled={addTodo.isPending}>
          {addTodo.isPending ? "Lägger till..." : "Lägg till"}
        </button>
      </form>
      {isLoading && <p>Laddar...</p>}
      <ul>
        {todos?.map((todo) => (
          <li key={todo.todoId}>
            <span>{todo.text}</span>
            <button
              onClick={() => deleteTodo.mutate(todo.todoId)}
              disabled={deleteTodo.isPending}
            >
              Ta bort
            </button>
          </li>
        ))}
      </ul>
      <button
        onClick={handleDeleteAccount}
        disabled={deleteAccount.isPending}
        style={{ color: "red" }}
      >
        {deleteAccount.isPending ? "Raderar..." : "Ta bort mitt konto"}
      </button>
    </div>
  );
}
