import { LucideIcon } from 'lucide-react';

export interface LinkItem {
  id: string;
  label: string;
  icon?: LucideIcon;
  href: string;
}

export interface ButtonItem {
  id: string;
  label: string;
  icon?: LucideIcon;
  onClick: VoidFunction;
}

export type NavbarMenuItem = LinkItem | ButtonItem;

export interface NavbarProps {
  items: NavbarMenuItem[];
}
