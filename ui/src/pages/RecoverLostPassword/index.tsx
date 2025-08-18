import type { FC } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { zodResolver } from '@hookform/resolvers/zod';
import { usePostPasswordresetForgot } from '@/api/apiComponents';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { UserRoundPlusIcon } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { ErrorMessage } from '@/components/ErrorMessage';

const schema = z.object({
  loginOrEmail: z
    .string()
    .min(3, 'Login or email is required')
    .refine((val) => z.email().safeParse(val).success || z.string().min(3), {
      message:
        'Must be a valid email or login (alphanumeric, at least 3 characters)',
    }),
});

const FORM_ID = 'recover-password-form';

export const RecoverLostPassword: FC = () => {
  const { mutate, error } = usePostPasswordresetForgot({
    onSuccess: () => {
      toast.success('Password recovery email sent successfully!');
    },
  });

  const form = useForm({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: {
      loginOrEmail: '',
    },
  });

  return (
    <div className="flex items-center justify-center h-full">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle>Recover Password</CardTitle>
          <CardDescription>
            Enter your email to recover your password
          </CardDescription>
          <CardAction>
            <UserRoundPlusIcon className="w-7 h-7 mt-2" />
          </CardAction>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              id={FORM_ID}
              className="grid grid-rows-1 gap-6"
              onSubmit={form.handleSubmit((data) => mutate({ body: data }))}
            >
              <FormField
                control={form.control}
                name="loginOrEmail"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Login or email</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </form>
          </Form>
        </CardContent>
        <CardFooter className="grid grid-rows-2 gap-4">
          <Button
            type="submit"
            form={FORM_ID}
            className="w-full"
            disabled={form.formState.isSubmitting}
          >
            Reset Password
          </Button>
          {error && <ErrorMessage error={error} />}
        </CardFooter>
      </Card>
    </div>
  );
};
