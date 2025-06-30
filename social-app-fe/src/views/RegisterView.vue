<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { toTypedSchema } from '@vee-validate/zod'
import * as z from 'zod'
import { Form, Field, ErrorMessage } from 'vee-validate'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

const authStore = useAuthStore()

// Schema validation cho form đăng ký
const registerSchema = toTypedSchema(
  z.object({
    email: z.string().min(1, 'Email không được để trống').email('Email không hợp lệ'),
    password: z.string().min(6, 'Mật khẩu phải có ít nhất 6 ký tự'),
    confirmPassword: z.string().min(1, 'Vui lòng xác nhận mật khẩu'),
  }).refine(data => data.password === data.confirmPassword, {
    message: "Mật khẩu không khớp",
    path: ["confirmPassword"], // Chỉ định lỗi này thuộc về trường confirmPassword
  })
)

// Hàm xử lý khi submit form đăng ký
async function handleRegister(values: any) {
  await authStore.register(values.email, values.password)
}
</script>

<template>
  <Card class="w-full max-w-sm">
    <CardHeader>
      <CardTitle class="text-2xl">
        Đăng ký
      </CardTitle>
      <CardDescription>
        Tạo tài khoản mới để tham gia mạng xã hội.
      </CardDescription>
    </CardHeader>
    <CardContent>
      <Form :validation-schema="registerSchema" @submit="handleRegister" class="grid gap-4">
        <div class="grid gap-2">
          <Label for="email">Email</Label>
          <Field name="email" v-slot="{ field }">
            <Input id="email" type="email" placeholder="m@example.com" v-bind="field" />
          </Field>
          <ErrorMessage name="email" class="text-red-500 text-sm" />
        </div>
        <div class="grid gap-2">
          <Label for="password">Mật khẩu</Label>
          <Field name="password" v-slot="{ field }">
            <Input id="password" type="password" v-bind="field" />
          </Field>
          <ErrorMessage name="password" class="text-red-500 text-sm" />
        </div>
        <div class="grid gap-2">
          <Label for="confirmPassword">Xác nhận mật khẩu</Label>
          <Field name="confirmPassword" v-slot="{ field }">
            <Input id="confirmPassword" type="password" v-bind="field" />
          </Field>
          <ErrorMessage name="confirmPassword" class="text-red-500 text-sm" />
        </div>

        <!-- Hiển thị lỗi từ API -->
        <p v-if="authStore.error" class="text-red-500 text-sm text-center">
          {{ authStore.error }}
        </p>

        <Button type="submit" class="w-full" :disabled="authStore.isLoading">
          <span v-if="authStore.isLoading">Đang xử lý...</span>
          <span v-else>Tạo tài khoản</span>
        </Button>
      </Form>

      <div class="mt-4 text-center text-sm">
        Đã có tài khoản?
        <router-link :to="{ name: 'Login' }" class="underline">
          Đăng nhập
        </router-link>
      </div>
    </CardContent>
  </Card>
</template>