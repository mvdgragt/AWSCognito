import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";

const BASE_URL = import.meta.env.VITE_API_URL;

export function getAuthHeaders() {
  const token = localStorage.getItem("accessToken");
  if (!token) {
    throw new Error("Not authenticated");
  }
  return {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };
}

export function useTodos() {
  return useQuery({
    queryKey: ["todos"],
    retry: false,
    queryFn: async () => {
      const headers = getAuthHeaders();
      const res = await fetch(`${BASE_URL}/todos`, { headers });
      if (!res.ok) throw new Error("Kunde inte hämta uppgifter");
      return res.json();
    },
  });
}

export function useAddTodo() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (text) => {
      const headers = getAuthHeaders();
      const res = await fetch(`${BASE_URL}/todos`, {
        method: "POST",
        headers,
        body: JSON.stringify({ text }),
      });
      if (!res.ok) throw new Error("Kunde inte skapa uppgift");
      return res.json();
    },
    onSuccess: (newTodo) => {
      queryClient.setQueryData(["todos"], (old) => [...(old || []), newTodo]);
    },
  });
}

export function useDeleteTodo() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (todoId) => {
      const headers = getAuthHeaders();
      const res = await fetch(`${BASE_URL}/todos/${todoId}`, {
        method: "DELETE",
        headers,
      });
      if (!res.ok) throw new Error("Kunde inte ta bort uppgift");
    },
    onSuccess: (_, todoId) => {
      queryClient.setQueryData(["todos"], (old) =>
          old.filter((t) => t.todoId !== todoId),
      );
    },
  });
}

export function useDeleteAccount() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async () => {
      const headers = getAuthHeaders();
      const res = await fetch(`${BASE_URL}/account`, {
        method: "DELETE",
        headers,
      });
      if (!res.ok) throw new Error("Kunde inte ta bort konto");
      localStorage.removeItem("accessToken");
      localStorage.removeItem("idToken");
      localStorage.removeItem("refreshToken");
    },
    onSuccess: () => {
      queryClient.clear();
    },
  });
}