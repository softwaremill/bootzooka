import { usePostUser } from '@/api/apiComponents';
import { useUserContext } from '@/contexts/UserContext/User.context';
import z from 'zod';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';

const schema = z.object({
  login: z.string().min(1, 'Login is required'),
  email: z.email('Invalid email address'),
});

export const ProfileDetails = () => {
  const {
    dispatch,
    state: { user },
  } = useUserContext();

  const mutation = usePostUser({
    onSuccess: (data) => {
      dispatch({ type: 'UPDATE_USER_DATA', user: data });
      toast('Profile details changed');
    },
  });

  const form = useForm({
    resolver: zodResolver(schema),
    mode: 'onBlur',
    defaultValues: {
      login: user?.login ?? '',
      email: user?.email ?? '',
    },
  });

  return (
    <Card>
      <CardHeader>
        <CardTitle>Profile details</CardTitle>
      </CardHeader>
      <CardContent>
        {user ? (
          <Form {...form}>
            <form
              id="profile-details"
              className="grid grid-rows-3 gap-6"
              onSubmit={form.handleSubmit((body) => mutation.mutate({ body }))}
            >
              <FormField
                control={form.control}
                name="login"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Login</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Email</FormLabel>
                    <FormControl>
                      <Input type="email" {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <Button
                type="submit"
                className="w-full"
                disabled={form.formState.isSubmitting}
              >
                Update profile details
              </Button>
            </form>
          </Form>
        ) : (
          <p>Profile details not available</p>
        )}
      </CardContent>
    </Card>
  );
};
